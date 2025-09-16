output "function_arn" {
  description = "ARN of the Lambda function"
  value       = aws_lambda_function.this.arn
}

output "function_name" {
  description = "Name of the Lambda function"
  value       = aws_lambda_function.this.function_name
}

output "alias_arn" {
  description = "ARN of the Lambda alias"
  value       = aws_lambda_alias.live.arn
}

output "invoke_arn" {
  description = "Invoke ARN of the Lambda alias"
  value       = aws_lambda_alias.live.invoke_arn
}

output "integration_id" {
  description = "API Gateway integration ID"
  value       = var.api_id != "" ? aws_apigatewayv2_integration.this[0].id : null
}

output "route_id" {
  description = "API Gateway route ID"
  value       = var.api_id != "" ? aws_apigatewayv2_route.this[0].id : null
}