// 退货退款 / 取消订单审核相关接口
// 统一带上 token 并解析后端 Result：HTTP 非 2xx 或 code !== 200 都当作失败返回 { ok, msg }

const parseResult = async (response) => {
  if (!response.ok) {
    return {
      ok: false,
      msg: response.status === 401 ? '请先登录' : `请求失败(HTTP ${response.status})`
    }
  }

  let result = null
  try {
    result = await response.json()
  } catch (e) {
    return { ok: false, msg: '服务响应异常，请确认后端已启动' }
  }

  if (result.code !== 200) {
    return { ok: false, msg: result.msg || '操作失败' }
  }
  return { ok: true, data: result.data }
}

const postJson = (url, body) => {
  const token = localStorage.getItem('token')
  return fetch(url, {
    method: 'POST',
    headers: {
      'token': token || '',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  }).then(parseResult)
}

// 买家：申请退货退款（仅待发货订单）
export const applyRefund = (orderNo, refundReason) =>
  postJson('/order/refund/apply', { orderNo, refundReason })

// 卖家：同意退货退款
export const approveRefund = (orderNo) =>
  postJson('/order/refund/approve', { orderNo })

// 卖家：拒绝退货退款
export const rejectRefund = (orderNo, rejectReason) =>
  postJson('/order/refund/reject', { orderNo, rejectReason })

// 买家：取消订单（待接单直接取消，待支付转成申请等待卖家审核）
export const applyCancel = (orderNo, cancelReason) =>
  postJson('/order/cancel', { orderNo, cancelReason })

// 卖家：同意取消订单
export const approveCancel = (orderNo) =>
  postJson('/order/cancel/approve', { orderNo })

// 卖家：拒绝取消订单
export const rejectCancel = (orderNo, rejectReason) =>
  postJson('/order/cancel/reject', { orderNo, rejectReason })
