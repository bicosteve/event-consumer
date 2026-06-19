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
resource "aws_key_pair" "this" {
  key_name   = "${var.name_prefix}-key"
  public_key = file(var.ssh_public_key_path)
}
