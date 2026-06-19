# Network module: looks up existing VPC/subnets/security groups + the AMI, and
# creates the SSH key pair. It does NOT create any networking — those already
# exist and are referenced by id.

variable "name_prefix" {
  description = "Prefix used for the key pair name."
  type        = string
}

variable "vpc_id" {
  description = "Existing VPC id."
  type        = string
}

variable "public_subnet_id" {
  description = "Existing PUBLIC subnet id."
  type        = string
}

variable "private_subnet_id" {
  description = "Existing PRIVATE subnet id."
  type        = string
}

variable "web_access_sg_id" {
  description = "Existing security group id for public instances."
  type        = string
}

variable "data_sg_id" {
  description = "Existing security group id for private instances."
  type        = string
}

variable "ssh_public_key_path" {
  description = "Path to the local SSH public key to install on instances."
  type        = string
}
