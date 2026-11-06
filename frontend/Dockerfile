FROM node:24-alpine

WORKDIR /app
ENV HUSKY=0
COPY package*.json ./
RUN npm install
COPY . .

EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host"]