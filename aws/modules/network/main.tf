# ---------------------------------------------------------------------------
# Look up existing networking: VPC, the public + private subnets, the two
# existing security groups (web_access_SG, data_SG), and the latest Amazon
# Linux 2023 AMI. Also create the SSH key pair installed on all VMs.
#
# NOTE: VPC, subnets and security groups are NOT created here — they already
# exist in your account and are referenced by id.
# ---------------------------------------------------------------------------

data "aws_vpc" "this" {
  id = var.vpc_id
}

data "aws_subnet" "public" {
  id = var.public_subnet_id
}

data "aws_subnet" "private" {
  id = var.private_subnet_id
}

# Existing security groups (looked up by id; not managed by this config).
data "aws_security_group" "web_access" {
  id = var.web_access_sg_id
}

data "aws_security_group" "data" {
  id = var.data_sg_id
}

# Latest Amazon Linux 2023 x86_64 AMI (works with t2.micro).
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
  filter {
    name   = "architecture"
    values = ["x86_64"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# SSH key pair installed on all instances.
#
# Two modes:
#   1. Reuse an EXISTING key pair (the one already on your other VMs):
#      set var.existing_key_name -> we just look it up, nothing is created.
#   2. Create a NEW key pair from a local .pub file:
#      leave existing_key_name null and set ssh_public_key_path.
locals {
  reuse_existing_key = var.existing_key_name != null
}

# Mode 1: look up the existing key pair so we can validate it exists.
data "aws_key_pair" "existing" {
  count    = local.reuse_existing_key ? 1 : 0
  key_name = var.existing_key_name
}

# Mode 2: create a new key pair from a local public key.
resource "aws_key_pair" "this" {
  count      = local.reuse_existing_key ? 0 : 1
  key_name   = "${var.name_prefix}-key"
  public_key = file(var.ssh_public_key_path)
}
