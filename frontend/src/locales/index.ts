import { createI18n } from 'vue-i18n'
import en from './en.json'
import zh from './zh.json'

const messages = {
  en,
  zh,
}

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'zh',
  fallbackLocale: 'en',
  messages,
})

document.documentElement.lang = i18n.global.locale.value === 'zh' ? 'zh-CN' : 'en'

export default i18n

export function setLocale(locale: 'zh' | 'en') {
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale)
  document.documentElement.lang = locale === 'zh' ? 'zh-CN' : 'en'
}

export function getLocale(): 'zh' | 'en' {
  return i18n.global.locale.value
}
