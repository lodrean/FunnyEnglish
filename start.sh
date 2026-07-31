#!/bin/bash
echo "Starting FunnyEnglish..."
docker compose up -d --build
echo ""
echo "Admin Panel: http://localhost:3000"
echo "Email: admin@funnyenglish.com"
echo "Password: admin123"
