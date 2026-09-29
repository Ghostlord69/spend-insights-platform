.PHONY: build up down test logs

build:
	cd backend && ./gradlew bootJar -x test
	cd frontend && npm ci && npm run build

up: build
	docker compose up --build -d

down:
	docker compose down

test:
	cd backend && ./gradlew test

logs:
	docker compose logs -f
