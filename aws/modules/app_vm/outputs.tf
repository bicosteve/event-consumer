output "id" {
  description = "Instance id."
  value       = aws_instance.this.id
}

output "private_ip" {
  description = "Private IP."
  value       = aws_instance.this.private_ip
}

output "public_ip" {
  description = "Public IP (empty if not assigned)."
  value       = aws_instance.this.public_ip
}
