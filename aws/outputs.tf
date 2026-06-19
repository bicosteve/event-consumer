output "api_gateway_public_ip" {
  description = "Public IP of the api-gateway VM (internet-facing REST API)."
  value       = module.api_gateway.public_ip
}

output "rapid_engine_public_ip" {
  description = "Public IP of the rapid-engine worker VM."
  value       = module.rapid_engine.public_ip
}

output "event_consumer_private_ip" {
  description = "Private IP of the event-consumer VM."
  value       = module.event_consumer.private_ip
}

output "data_private_ip" {
  description = "Private IP of the data VM (MySQL/RabbitMQ/Redis). Used by all apps."
  value       = module.data.private_ip
}

output "api_gateway_url" {
  description = "Convenience URL for the api-gateway."
  value       = "http://${module.api_gateway.public_ip}:${var.api_gateway_port}"
}

output "ssh_api_gateway" {
  description = "SSH into the public api-gateway VM."
  value       = "ssh ec2-user@${module.api_gateway.public_ip}"
}

output "ssh_data_via_bastion" {
  description = "SSH to the private data VM via the api-gateway as a bastion."
  value       = "ssh -J ec2-user@${module.api_gateway.public_ip} ec2-user@${module.data.private_ip}"
}
