import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Sell from '../views/Sell.vue'
import Messages from '../views/Messages.vue'
import Profile from '../views/Profile.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import EditProfile from '../views/EditProfile.vue'
import ChatRoom from '../views/ChatRoom.vue'
import MyProducts from '../views/MyProducts.vue'
import ProductDetail from '../views/ProductDetail.vue'
import EditProduct from '../views/EditProduct.vue'
import MyFavorites from '../views/MyFavorites.vue'
import MyOrders from '../views/MyOrders.vue'
import OrderDetail from '../views/OrderDetail.vue'
import CreateOrder from '../views/CreateOrder.vue'
import MyWallet from '../views/MyWallet.vue'
import Settings from '../views/Settings.vue'
import MyAddress from '../views/MyAddress.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/sell', component: Sell },
  { path: '/messages', component: Messages },
  { path: '/messages/chat', component: ChatRoom },
  { path: '/profile', component: Profile },
  { path: '/profile/edit', component: EditProfile },
  { path: '/my-products', component: MyProducts },
  { path: '/my-favorites', component: MyFavorites },
  { path: '/my-orders', component: MyOrders },
  { path: '/order-detail/:orderNo', component: OrderDetail },
  { path: '/order/create/:productId', component: CreateOrder },
  { path: '/my-wallet', component: MyWallet },
  { path: '/settings', component: Settings },
  { path: '/my-address', component: MyAddress },
  { path: '/product/edit/:id', component: EditProduct },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/product/:id', component: ProductDetail }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router