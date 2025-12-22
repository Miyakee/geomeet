#!/bin/bash
# 构建镜像并推送到 ECR
# 在本地机器或 CI/CD 中运行

set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 配置
AWS_REGION="${AWS_REGION:-ap-southeast-1}"
AWS_PROFILE="${AWS_PROFILE:-tw-esg}"
PROJECT_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"

echo "🚀 构建并推送镜像到 ECR..."

# 获取 AWS 账户 ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text --profile $AWS_PROFILE 2>/dev/null || echo "")
if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo -e "${RED}❌ 无法获取 AWS 账户 ID，请检查 AWS 凭证${NC}"
    exit 1
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo -e "${YELLOW}ECR Registry: $ECR_REGISTRY${NC}"

# 检查 ECR 仓库是否存在
echo -e "${YELLOW}检查 ECR 仓库...${NC}"
if ! aws ecr describe-repositories --repository-names geomeet-api --region $AWS_REGION --profile $AWS_PROFILE &>/dev/null; then
    echo -e "${YELLOW}创建 ECR 仓库...${NC}"
    aws ecr create-repository --repository-name geomeet-api --region $AWS_REGION --profile $AWS_PROFILE
    aws ecr create-repository --repository-name geomeet-ui --region $AWS_REGION --profile $AWS_PROFILE
fi

# 登录到 ECR
echo -e "${YELLOW}登录到 ECR...${NC}"
aws ecr get-login-password --region $AWS_REGION --profile $AWS_PROFILE | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

echo -e "${GREEN}✅ ECR 登录成功${NC}"
echo ""

# 启用 BuildKit
export DOCKER_BUILDKIT=1

# 构建后端镜像
echo -e "${YELLOW}📦 构建后端镜像...${NC}"
cd $PROJECT_ROOT/api

# 获取 Git commit SHA（用于版本标签）
GIT_SHA=$(git rev-parse --short HEAD 2>/dev/null || echo "latest")

docker build -t geomeet-api:latest .
docker tag geomeet-api:latest $ECR_REGISTRY/geomeet-api:latest
docker tag geomeet-api:latest $ECR_REGISTRY/geomeet-api:$GIT_SHA

# 推送后端镜像
echo -e "${YELLOW}📤 推送后端镜像到 ECR...${NC}"
docker push $ECR_REGISTRY/geomeet-api:latest
docker push $ECR_REGISTRY/geomeet-api:$GIT_SHA

echo -e "${GREEN}✅ 后端镜像推送完成${NC}"
echo ""

# 构建前端镜像
echo -e "${YELLOW}📦 构建前端镜像...${NC}"
cd $PROJECT_ROOT/ui

docker build -t geomeet-ui:latest .
docker tag geomeet-ui:latest $ECR_REGISTRY/geomeet-ui:latest
docker tag geomeet-ui:latest $ECR_REGISTRY/geomeet-ui:$GIT_SHA

# 推送前端镜像
echo -e "${YELLOW}📤 推送前端镜像到 ECR...${NC}"
docker push $ECR_REGISTRY/geomeet-ui:latest
docker push $ECR_REGISTRY/geomeet-ui:$GIT_SHA

echo -e "${GREEN}✅ 前端镜像推送完成${NC}"
echo ""

echo -e "${GREEN}✅ 所有镜像推送完成！${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "镜像地址："
echo "  API: $ECR_REGISTRY/geomeet-api:latest"
echo "  API: $ECR_REGISTRY/geomeet-api:$GIT_SHA"
echo "  UI:  $ECR_REGISTRY/geomeet-ui:latest"
echo "  UI:  $ECR_REGISTRY/geomeet-ui:$GIT_SHA"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

