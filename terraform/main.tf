terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    bucket = "dev-pedroenlanube-serverless-web-terraform-state"
    key    = "serverless-back/terraform.tfstate"
    region = "eu-west-1"
  }
}

provider "aws" {
  region = var.aws_region
}

# Infraestructura compartida
module "shared_infrastructure" {
  source = "./modules/shared-infrastructure"
  
  environment = var.environment
  project_name = var.project_name
}

# Cognito Integration Service
module "cognito_integration_service" {
  source = "./cognito-integration-service"
  
  api_id = module.shared_infrastructure.api_id
  api_execution_arn = module.shared_infrastructure.api_execution_arn
  users_table_name = module.shared_infrastructure.users_table_name
  users_table_arn = module.shared_infrastructure.users_table_arn
  environment = var.environment
  project_name = var.project_name
}