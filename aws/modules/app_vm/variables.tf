# Generic single-container application VM.

variable "name" {
  description = "Resource name (e.g. sportsbook-api-gateway)."
  type        = string
}

variable "container_name" {
  description = "Docker container name (e.g. api-gateway)."
  type        = string
}

variable "image" {
  description = "Docker image to run (with tag)."
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
  description = "Subnet id to launch into."
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

variable "associate_public_ip" {
  description = "Whether to assign a public IP."
  type        = bool
  default     = false
}

variable "root_volume_gb" {
  description = "Root EBS volume size (GiB)."
  type        = number
  default     = 8
}

variable "port" {
  description = "Container/host port to publish. Null = no published port (worker)."
  type        = number
  default     = null
}

variable "env_file" {
  description = "Full body of the container's --env-file."
  type        = string
}

variable "dockerhub_username" {
  description = "Docker Hub username (only for private images)."
  type        = string
  default     = null
}

variable "dockerhub_token" {
  description = "Docker Hub token/password (only for private images)."
  type        = string
  sensitive   = true
  default     = null
}

variable "tags" {
  description = "Extra tags."
  type        = map(string)
  default     = {}
}
