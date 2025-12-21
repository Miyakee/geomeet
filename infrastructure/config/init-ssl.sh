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

# 检查 nginx 配置是否已包含 HTTPS（证书已配置）
if grep -q "ssl_certificate /etc/letsencrypt/live" nginx.conf 2>/dev/null; then
    echo "检测到 nginx.conf 已配置 HTTPS"
    # 检查证书是否已存在
    if docker-compose exec -T certbot test -f /etc/letsencrypt/live/$DOMAIN/fullchain.pem 2>/dev/null; then
        echo "证书已存在，跳过证书获取"
        echo "如果证书已过期，请使用: docker-compose run --rm certbot renew"
        exit 0
    fi
fi

# 检查 docker-compose 是否运行
if ! docker-compose ps | grep -q "geomeet-nginx"; then
    echo "警告: nginx 容器未运行，正在启动..."
    docker-compose up -d nginx
    echo "等待 nginx 启动..."
    sleep 5
fi

# 确保 certbot 容器可以访问共享卷
echo "检查 certbot volumes..."

# 获取证书
echo "正在获取 Let's Encrypt 证书..."
echo "注意: 确保域名 $DOMAIN 已正确指向此 EC2 实例的 IP 地址"
echo ""

docker-compose run --rm certbot certonly \
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
    echo "重新加载 nginx 配置..."
    docker-compose exec nginx nginx -t && docker-compose exec nginx nginx -s reload || docker-compose restart nginx
    
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

