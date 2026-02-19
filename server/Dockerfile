#---------------------build web------------------------
FROM docker.io/library/node:22-alpine AS build_web
WORKDIR /app/web
COPY web/package.json web/pnpm-lock.yaml ./
RUN npm install -g pnpm && pnpm install --frozen-lockfile
COPY web/ ./
RUN pnpm run build

# ---------------------build go--------------------
FROM golang:1.24.13-trixie AS builder_go
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod tidy
COPY . .
COPY --from=build_web /app/web/dist /app/web/dist

# 安装 ent 与 swagger（swag）
RUN go install entgo.io/ent/cmd/ent@latest
RUN go install github.com/swaggo/swag/cmd/swag@latest
RUN go generate ./...

RUN CGO_ENABLED=1 GOOS=linux GOARCH=amd64 go build -ldflags="-s -w -X main.version=rolling" -o shadmin-runner

#-----------------upx shadmin-runner -----------------
FROM backplane/upx:latest AS compressor
WORKDIR /app
COPY --from=builder_go /app/.env.example .
COPY --from=builder_go /app/shadmin-runner .
RUN upx --best --lzma shadmin-runner

#-------------------runner on debian (sqlite)--------------------------
FROM debian:trixie-slim AS runner
WORKDIR /app
COPY --from=compressor /app/shadmin-runner .
COPY --from=builder_go /app/.env.example .
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates && \
    rm -rf /var/lib/apt/lists/*
EXPOSE 55667
CMD ["./shadmin-runner"]

