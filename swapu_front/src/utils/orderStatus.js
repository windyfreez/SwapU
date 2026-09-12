// 订单状态字典：列表页与详情页共用一份，新增状态只改这里
// 与后端 Order 常量保持一致：1待确认 2待支付 3待发货 4待收货 5已收货 6取消订单 7退货审核中 8已退货退款 9取消申请中
export const ORDER_STATUS = {
  WAIT_ACCEPT: 1,
  WAIT_PAY: 2,
  WAIT_DELIVER: 3,
  WAIT_RECEIVE: 4,
  ALREADY_RECEIVE: 5,
  CANCEL: 6,
  REFUND_APPLYING: 7,
  REFUNDED: 8,
  CANCEL_APPLYING: 9
}

const STATUS_TEXT = {
  1: '待确认订单',
  2: '待支付',
  3: '待发货',
  4: '待收货',
  5: '已收货',
  6: '取消订单',
  7: '退货审核中',
  8: '已退货退款',
  9: '取消申请中'
}

const STATUS_CLASS = {
  1: 'status-pending',
  2: 'status-wait-pay',
  3: 'status-wait-deliver',
  4: 'status-wait-receive',
  5: 'status-completed',
  6: 'status-cancelled',
  7: 'status-refund-apply',
  8: 'status-refunded',
  9: 'status-cancel-apply'
}

// 优先用后端返回的 statusDesc，缺失时按状态码兜底（订单列表接口不返回 statusDesc）
export const getStatusText = (order) => {
  if (!order) return ''
  if (order.statusDesc) return order.statusDesc
  return STATUS_TEXT[order.status] || '未知状态'
}

export const getStatusClass = (status) => STATUS_CLASS[status] || ''

// 买家侧：等待卖家审核的两个状态
export const isWaitingAudit = (status) =>
  status === ORDER_STATUS.REFUND_APPLYING || status === ORDER_STATUS.CANCEL_APPLYING
