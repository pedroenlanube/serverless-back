# Data sources
data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

# Lambda Function
resource "aws_lambda_function" "this" {
  function_name = var.function_name
  role         = aws_iam_role.lambda_role.arn
  handler      = var.handler
  runtime      = var.runtime
  timeout      = var.timeout
  memory_size  = var.memory_size
  architectures = ["arm64"]
  publish      = true
  
  filename         = var.zip_file
  source_code_hash = filebase64sha256(var.zip_file)
  
  environment {
    variables = var.environment_variables
  }
  
  snap_start {
    apply_on = "PublishedVersions"
  }
  
  tags = var.tags
  
  depends_on = [
    aws_iam_role_policy_attachment.lambda_basic,
    aws_cloudwatch_log_group.lambda_logs,
  ]
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "lambda_logs" {
  name              = "/aws/lambda/${var.function_name}"
  retention_in_days = 14
  
  tags = var.tags
}

# Lambda Alias
resource "aws_lambda_alias" "live" {
  name             = "live"
  description      = "Live alias"
  function_name    = aws_lambda_function.this.function_name
  function_version = aws_lambda_function.this.version
}

# IAM Role for Lambda
resource "aws_iam_role" "lambda_role" {
  name = "${var.function_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
  
  tags = var.tags
}

# Basic Lambda execution policy
resource "aws_iam_role_policy_attachment" "lambda_basic" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_role.name
}

# Custom policies
resource "aws_iam_role_policy" "lambda_custom" {
  count = length(var.custom_policies) > 0 ? 1 : 0
  name  = "${var.function_name}-custom-policy"
  role  = aws_iam_role.lambda_role.id
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = var.custom_policies
  })
}

# API Gateway Integration (only if api_id is provided)
resource "aws_apigatewayv2_integration" "this" {
  count = var.api_id != "" ? 1 : 0
  
  api_id           = var.api_id
  integration_type = "AWS_PROXY"
  integration_uri  = aws_lambda_alias.live.invoke_arn
  
  payload_format_version = "2.0"
}

# API Gateway Route (only if api_id is provided)
resource "aws_apigatewayv2_route" "this" {
  count = var.api_id != "" ? 1 : 0
  
  api_id    = var.api_id
  route_key = "${var.http_method} ${var.route_path}"
  target    = "integrations/${aws_apigatewayv2_integration.this[0].id}"
}

# Lambda Permission for API Gateway (only if api_id is provided)
resource "aws_lambda_permission" "api_gateway" {
  count = var.api_id != "" ? 1 : 0
  
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_alias.live.function_name
  qualifier     = aws_lambda_alias.live.name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${var.api_execution_arn}/*/*"
}