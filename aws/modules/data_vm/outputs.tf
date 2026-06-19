output "id" {
  description = "Instance id."
  value       = aws_instance.this.id
}

output "private_ip" {
  description = "Private IP of the data VM."
  value       = aws_instance.this.private_ip
}

output "data_volume_id" {
  description = "Id of the dedicated data EBS volume."
  value       = aws_ebs_volume.data.id
}
