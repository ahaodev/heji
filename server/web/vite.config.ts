import path from 'path'
import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from '@tailwindcss/vite'
import {tanstackRouter} from '@tanstack/router-plugin/vite'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        tanstackRouter({
            target: 'react',
            autoCodeSplitting: true,
        }),
        react(),
        tailwindcss(),
    ],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    server: {
        host: '::',
        //open: true,
        port: 5173,
        strictPort: true,
        allowedHosts: ['.ahaodev.com', '.ucorg.com', 'localhost'],
        proxy: {
            '/api': {
                target: 'http://localhost:56789',
                changeOrigin: true,
                rewrite: (path) => path,
            },
            '/share': {
                target: 'http://localhost:56789',
                changeOrigin: true,
                rewrite: (path) => path,
            }
        },
    },
})
