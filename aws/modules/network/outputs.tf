output "vpc_id" {
  description = "Looked-up VPC id."
  value       = data.aws_vpc.this.id
}

output "public_subnet_id" {
  description = "Public subnet id."
  value       = data.aws_subnet.public.id
}

output "private_subnet_id" {
  description = "Private subnet id."
  value       = data.aws_subnet.private.id
}

output "private_subnet_az" {
  description = "Availability zone of the private subnet (for the data EBS volume)."
  value       = data.aws_subnet.private.availability_zone
}

output "web_access_sg_id" {
  description = "Security group id for public instances."
  value       = data.aws_security_group.web_access.id
}

output "data_sg_id" {
  description = "Security group id for private instances."
  value       = data.aws_security_group.data.id
}

output "ami_id" {
  description = "Latest Amazon Linux 2023 AMI id."
  value       = data.aws_ami.al2023.id
}

output "key_name" {
  description = "SSH key pair name attached to the instances (reused existing one, or the newly created one)."
  value = local.reuse_existing_key ? (
    data.aws_key_pair.existing[0].key_name
  ) : aws_key_pair.this[0].key_name
}
