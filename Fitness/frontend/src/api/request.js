/**
 * 用户端请求模块
 * 简化版本，用于 GitHub Pages 演示
 */

// 模拟 HTTP 请求方法
export async function get(url, config = {}) {
  console.log(`[API] GET ${url}`, config)
  return { data: null, status: 200 }
}

export async function post(url, data = {}, config = {}) {
  console.log(`[API] POST ${url}`, data, config)
  return { data: null, status: 200 }
}

export async function put(url, data = {}, config = {}) {
  console.log(`[API] PUT ${url}`, data, config)
  return { data: null, status: 200 }
}

export async function del(url, config = {}) {
  console.log(`[API] DELETE ${url}`, config)
  return { data: null, status: 200 }
}

// 默认导出
export default {
  get,
  post,
  put,
  delete: del
}
