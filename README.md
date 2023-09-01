# FileMasterManager

This is a cloud storage service.

# Stack:

## Frontend:

- Next.js
- Tailwind CSS
- Formik and Yup

## Backend:

- Spring Framework
- PostgreSQL database
- Hibernate ORM
- JWT tokens
- JUnit (for Unit-testing)

## How to run this app on your computer:

- Clone this repository
- Make sure you have opened PostgreSQL instance on your computer on port **5432**
- Go to the `/server/FileServer` folder
- Create new folder named `userfiles`
- Open `src/main/resources/hibernate.cfg.xml` file and provide your database username, password, name of your PostgreSQL database
- Run `mvn org.springframework.boot:spring-boot-maven-plugin:run` (make sure to be in `server/FileServer` folder)
- Go to the `/client` folder and run `npm install`, then run `npm run dev`
  <br/>
  <br/>
  Now, you should have your app opened on port 3000, so now you can open http://localhost:3000/ link in your browser.

## Screenshots:

![Screenshot_1](https://github.com/StellarLis/WebBlog/assets/86295320/80d2be5b-c597-4828-95c0-32432ea1429a)
![Screenshot_2](https://github.com/StellarLis/WebBlog/assets/86295320/25b80766-eed3-45a7-bfa0-db5e9235307a)
