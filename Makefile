OUT=tfplan

run:
	mvn spring-boot:run

install:
	mvn clean install

tests:
	./mvnw test -DfailIfNoTests=false


# ===== Docker =====


# ==== Terraform =====
init:
	cd aws && terraform init

validate:
	cd aws && terraform validate

plan:
	cd aws && terraform plan -out=$(OUT)

apply:
	cd aws && terraform apply $(OUT)

show:
	cd aws && terraform show

destroy:
	cd aws && terraform destroy
