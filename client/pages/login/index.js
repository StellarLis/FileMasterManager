import Head from "next/head";
import * as yup from 'yup';
import { Formik, Form, Field } from 'formik';
import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/router";
import axios from "axios";

const Login = () => {

    const router = useRouter();

    const loginSchema = yup.object().shape({
        username: yup.string().required("Required").min(8, "Username should be 8 characters minimum")
            .max(28, "Username should be 28 characters maximum")
            .matches(/^[^.]+$/, "Username shouldn't contain a dot"),
        password: yup.string().required("Required").min(6, "Password should be 6 characters minimum")
            .max(28, "Password should be 28 characters maximum")
    });

    const [serverError, setServerError] = useState(null);

    const login = (username, password) => {
        axios.post(`http://localhost:8080/auth/login`, {
            username, password
        }).then(response => {
            const token = response.data.token;
            localStorage.setItem('token', token);
            router.push('/');
        }).catch(() => {
            setServerError('Invalid email or password')
        });
    }

    return (
        <>
            <Head>
                <title>FileMasterManager | Login</title>
            </Head>
            <div className='grid place-items-center bg-gray-800 min-h-screen text-white'>
                <Formik
                    initialValues={{
                        username: '',
                        password: ''
                    }}
                    onSubmit={values => login(values.username, values.password)}
                    validationSchema={loginSchema}
                >
                    {({ errors, touched }) => (
                        <Form className='flex flex-col'>
                            <h1 className='text-2xl self-center'>Login</h1>
                            <label className='mt-4'>Username</label>
                            <Field className='w-96 bg-gray-800 border-2 rounded-lg border-gray-600 p-2 transition outline-none focus:border-blue-500 focus:border-2' name="username" placeholder="Your username" />
                            {errors.username && touched.username && (
                                <p className='text-red-500'>{errors.username}</p>
                            )}
                            <label className='mt-4'>Password</label>
                            <Field className='w-96 bg-gray-800 border-2 rounded-lg border-gray-600 p-2 transition outline-none focus:border-blue-500 focus:border-2'
                                name="password" placeholder='Your password' type="password" />
                            {errors.password && touched.password && (
                                <div className='text-red-500'>{errors.password}</div>
                            )}
                            <button className="bg-gray-700 mt-2 pt-2 pb-2 rounded-md transition hover:bg-gray-600"
                                type='submit'>Submit</button>
                            <p className='self-center mt-1'>Don't have an account? <Link href='/register' className='text-blue-500 transition hover:text-blue-400'>Create one!</Link></p>
                            <p className='mt-1 text-red-500 text-xl self-center font-bold'>{serverError}</p>
                        </Form>
                    )
                    }
                </Formik>
            </div>
        </>
    );
}

export default Login;