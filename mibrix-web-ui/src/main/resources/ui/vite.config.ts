import react, {reactCompilerPreset} from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import {defineConfig} from 'vite'

// https://vite.dev/config/
export default defineConfig(({command, mode}) => {

    console.log("当前命令: ", command, " 当前模式: ", mode)

    return {
        base: "./",

        plugins: [
            react(),
            babel({presets: [reactCompilerPreset()]})
        ],

        build: {
            outDir: "dist",
            rolldownOptions: {}
        }
    }
})
