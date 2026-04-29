import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/user'

const storage = sessionStorage

export const useUserStore = defineStore('user', () => {
  const token = ref(storage.getItem('token') || '')
  const userId = ref(storage.getItem('userId') || '')
  const username = ref(storage.getItem('username') || '')
  const realName = ref(storage.getItem('realName') || '')
  const role = ref(storage.getItem('role') || '')

  function setLoginInfo(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    realName.value = data.realName
    role.value = data.role
    storage.setItem('token', data.token)
    storage.setItem('userId', data.userId)
    storage.setItem('username', data.username)
    storage.setItem('realName', data.realName)
    storage.setItem('role', data.role)
  }

  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    storage.removeItem('token')
    storage.removeItem('userId')
    storage.removeItem('username')
    storage.removeItem('realName')
    storage.removeItem('role')
  }

  async function fetchUserInfo() {
    const res = await getUserInfo()
    if (res.code === 200) {
      realName.value = res.data.realName
      role.value = res.data.role
      storage.setItem('realName', res.data.realName)
      storage.setItem('role', res.data.role)
    }
  }

  return { token, userId, username, realName, role, setLoginInfo, logout, fetchUserInfo }
})
