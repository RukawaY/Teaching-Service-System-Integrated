import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './assets/main.css'
import axios from 'axios'
import ElementPlus from 'element-plus'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { library } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";
import { fab } from "@fortawesome/free-brands-svg-icons";
import "@fortawesome/fontawesome-svg-core/styles.css";

const app = createApp(App)
const pinia = createPinia()
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

library.add(
    ...Object.values(fas),
    ...Object.values(far),
    ...Object.values(fab)
);

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
app.component("FontAwesomeIcon", FontAwesomeIcon)

