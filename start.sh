#!/bin/bash
echo "Starting So to Speak..."
docker compose up -d --build
echo ""
echo "Admin Panel: http://localhost:3000"
echo "Email: admin@sotospeak.com"
echo "Password: admin123"
