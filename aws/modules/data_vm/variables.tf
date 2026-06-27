# Data VM: MySQL + RabbitMQ + Redis installed natively, with a dedicated EBS volume.

variable "name" {
  description = "Resource name (e.g. sportsbook-data)."
  type        = string
}

variable "ami_id" {
  description = "AMI id."
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type."
  type        = string
  default     = "t2.micro"
}

variable "subnet_id" {
  description = "Subnet id (should be private)."
  type        = string
}

variable "availability_zone" {
  description = "AZ of the subnet (for the EBS volume)."
  type        = string
}

variable "security_group_ids" {
  description = "Security group ids to attach."
  type        = list(string)
}

variable "key_name" {
  description = "EC2 key pair name."
  type        = string
}

variable "root_volume_gb" {
  description = "Root EBS volume size (GiB)."
  type        = number
  default     = 8
}

variable "data_volume_gb" {
  description = "Dedicated data EBS volume size (GiB)."
  type        = number
  default     = 20
}

variable "mysql_root_password" {
  type      = string
  sensitive = true
}

variable "db_name" {
  type = string
}

variable "db_username" {
  type = string
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "rabbitmq_username" {
  type = string
}

variable "rabbitmq_password" {
  type      = string
  sensitive = true
}

variable "rabbitmq_vhost" {
  type = string
}

variable "redis_password" {
  type      = string
  sensitive = true
  default   = ""
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
