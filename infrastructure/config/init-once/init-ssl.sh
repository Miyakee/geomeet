#!/bin/bash

# SSL 证书初始化脚本
# 用于首次获取 Let's Encrypt 证书
# 注意：此脚本需要在 EC2 上执行，因为 Let's Encrypt 需要通过 HTTP-01 验证

set -e

DOMAIN="ttyuuuuuuuuuuuu.us.ci"
EMAIL="miyakee27@163.com"  # 请修改为您的邮箱

echo "=========================================="
echo "SSL 证书初始化脚本"
echo "域名: $DOMAIN"
echo "=========================================="

# 检查是否在正确的目录
if [ ! -f "docker-compose.yml" ]; then
    echo "错误: 请在包含 docker-compose.yml 的目录下执行此脚本"
    exit 1
fi

# 使用 docker-compose 或 docker compose
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
else
    COMPOSE_CMD="docker compose"
fi

# 检查 nginx 配置是否已包含 HTTPS（证书已配置）
if grep -q "ssl_certificate /etc/letsencrypt/live" nginx.conf 2>/dev/null; then
    echo "检测到 nginx.conf 已配置 HTTPS"
    # 检查证书是否已存在
    if $COMPOSE_CMD exec -T certbot test -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem 2>/dev/null; then
        echo "证书已存在，跳过证书获取"
        echo "如果证书已过期，请使用: $COMPOSE_CMD run --rm certbot renew"
        exit 0
    fi
fi

# 检查证书是否已存在（避免重复获取）
# 使用临时容器检查，避免依赖正在运行的 certbot 容器
if $COMPOSE_CMD run --rm --entrypoint="" certbot test -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem 2>/dev/null; then
    echo "✅ 证书已存在: /etc/letsencrypt/live/$DOMAIN/fullchain.pem"
    echo "如果证书已过期，请使用: $COMPOSE_CMD run --rm certbot renew"
    echo "查看证书信息: $COMPOSE_CMD run --rm certbot certificates"
    exit 0
fi

# 证书不存在，需要切换到 HTTP-only 配置以获取证书
echo "证书不存在，切换到 HTTP-only 配置以获取证书..."

# 备份当前配置（如果包含 HTTPS 配置）
if grep -q "ssl_certificate" nginx.conf 2>/dev/null; then
    if [ ! -f "nginx.conf.backup" ]; then
        cp nginx.conf nginx.conf.backup
        echo "已备份当前 nginx.conf 为 nginx.conf.backup"
    fi
fi

# 使用 HTTP-only 配置
if [ -f "nginx.conf.http-only" ]; then
    cp nginx.conf.http-only nginx.conf
    echo "已切换到 HTTP-only 配置"
else
    echo "警告: nginx.conf.http-only 不存在，尝试继续使用当前配置"
fi

# 检查 docker-compose 是否运行
if ! $COMPOSE_CMD ps | grep -q "geomeet-ui"; then
    echo "警告: nginx 容器未运行，正在启动..."
    $COMPOSE_CMD up -d nginx 2>/dev/null || $COMPOSE_CMD up -d 2>/dev/null || true
    echo "等待 nginx 启动..."
    sleep 5
else
    # 重新加载 nginx 配置
    echo "重新加载 nginx 配置..."
    $COMPOSE_CMD exec geomeet-ui nginx -t 2>/dev/null && $COMPOSE_CMD exec geomeet-ui nginx -s reload 2>/dev/null || \
    $COMPOSE_CMD exec nginx nginx -t 2>/dev/null && $COMPOSE_CMD exec nginx nginx -s reload 2>/dev/null || \
    $COMPOSE_CMD restart nginx 2>/dev/null || $COMPOSE_CMD restart geomeet-ui 2>/dev/null || true
    sleep 3
fi

# 确保 certbot 容器可以访问共享卷
echo "检查 certbot volumes..."

# 获取证书
echo "正在获取 Let's Encrypt 证书..."
echo "注意: 确保域名 $DOMAIN 已正确指向此 EC2 实例的 IP 地址"
echo "注意: 如果看到 'No renewals were attempted'，这是正常的，因为我们正在首次获取证书"
echo ""

# 使用 --rm 创建临时容器来获取证书（不会影响正在运行的 certbot 容器）
$COMPOSE_CMD run --rm --entrypoint="" certbot certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    -d "$DOMAIN"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 证书获取成功！"
    echo ""
    
    # 恢复完整的 HTTPS 配置
    if [ -f "nginx.conf.backup" ]; then
        echo "恢复完整的 HTTPS 配置..."
        cp nginx.conf.backup nginx.conf
        echo "已恢复 nginx.conf"
    elif [ -f "nginx.conf.http-only" ]; then
        echo "警告: 未找到备份配置，请手动更新 nginx.conf 以启用 HTTPS"
    fi
    
    echo "重新加载 nginx 配置..."
    $COMPOSE_CMD exec nginx nginx -t 2>/dev/null && $COMPOSE_CMD exec nginx nginx -s reload 2>/dev/null || \
    $COMPOSE_CMD exec geomeet-ui nginx -t 2>/dev/null && $COMPOSE_CMD exec geomeet-ui nginx -s reload 2>/dev/null || \
    $COMPOSE_CMD restart nginx 2>/dev/null || $COMPOSE_CMD restart geomeet-ui 2>/dev/null || true
    
    echo ""
    echo "=========================================="
    echo "SSL 证书初始化完成！"
    echo "证书位置: /etc/letsencrypt/live/$DOMAIN/"
    echo ""
    echo "现在可以通过以下地址访问:"
    echo "  https://$DOMAIN"
    echo "=========================================="
else
    echo ""
    echo "❌ 证书获取失败！"
    echo "请检查："
    echo "  1. 域名 $DOMAIN 是否正确指向此 EC2 实例"
    echo "  2. EC2 安全组是否允许 80 和 443 端口入站"
    echo "  3. nginx 容器是否正常运行"
    echo "  4. 域名解析是否生效（可能需要等待几分钟）"
    exit 1
fi

