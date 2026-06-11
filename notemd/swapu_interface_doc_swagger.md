# SwapU云市集项目接口文档


**简介**:SwapU云市集项目接口文档


**HOST**:localhost:8080


**联系人**:


**Version**:1.0


**接口路径**:/v2/api-docs?group=用户端接口


[TOC]






# 商品分类接口


## 获取所有分类


**接口地址**:`/category/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«List«Category»»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||array|Category|
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;createUser||integer(int64)||
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;name||string||
|&emsp;&emsp;sort||integer(int32)||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;updateUser||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": [
		{
			"createTime": "",
			"createUser": 0,
			"id": 0,
			"name": "",
			"sort": 0,
			"status": 0,
			"updateTime": "",
			"updateUser": 0
		}
	],
	"msg": ""
}
```


# 商品接口


## 添加商品


**接口地址**:`/product`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "categoryId": 0,
  "description": "",
  "id": 0,
  "images": [],
  "originalPrice": 0,
  "price": 0,
  "productCondition": 0,
  "quantity": 0,
  "title": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|productDTO|productDTO|body|true|ProductDTO|ProductDTO|
|&emsp;&emsp;categoryId|||false|integer(int32)||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;images|||false|array|string|
|&emsp;&emsp;originalPrice|||false|number||
|&emsp;&emsp;price|||false|number||
|&emsp;&emsp;productCondition|||false|integer(int32)||
|&emsp;&emsp;quantity|||false|integer(int32)||
|&emsp;&emsp;title|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«ProductVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||ProductVO|ProductVO|
|&emsp;&emsp;productId||integer(int64)||
|&emsp;&emsp;status||integer(int32)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"productId": 0,
		"status": 0
	},
	"msg": ""
}
```


## 修改商品信息


**接口地址**:`/product`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "categoryId": 0,
  "description": "",
  "id": 0,
  "images": [],
  "originalPrice": 0,
  "price": 0,
  "productCondition": 0,
  "quantity": 0,
  "title": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|productDTO|productDTO|body|true|ProductDTO|ProductDTO|
|&emsp;&emsp;categoryId|||false|integer(int32)||
|&emsp;&emsp;description|||false|string||
|&emsp;&emsp;id|||false|integer(int64)||
|&emsp;&emsp;images|||false|array|string|
|&emsp;&emsp;originalPrice|||false|number||
|&emsp;&emsp;price|||false|number||
|&emsp;&emsp;productCondition|||false|integer(int32)||
|&emsp;&emsp;quantity|||false|integer(int32)||
|&emsp;&emsp;title|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 根据id获取商品信息


**接口地址**:`/product/detail/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«ProductDetailVO»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||ProductDetailVO|ProductDetailVO|
|&emsp;&emsp;categoryId||integer(int32)||
|&emsp;&emsp;categoryName||string||
|&emsp;&emsp;contactWay||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;description||string||
|&emsp;&emsp;favoriteCount||integer(int32)||
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;images||array|string|
|&emsp;&emsp;isFavorite||boolean||
|&emsp;&emsp;originalPrice||number||
|&emsp;&emsp;price||number||
|&emsp;&emsp;productCondition||integer(int32)||
|&emsp;&emsp;productConditionDesc||string||
|&emsp;&emsp;quantity||integer(int32)||
|&emsp;&emsp;sellerInfo||SellerInfo|SellerInfo|
|&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;creditScore||integer||
|&emsp;&emsp;&emsp;&emsp;id||integer||
|&emsp;&emsp;&emsp;&emsp;publishCount||integer||
|&emsp;&emsp;&emsp;&emsp;soldCount||integer||
|&emsp;&emsp;&emsp;&emsp;username||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|&emsp;&emsp;title||string||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;viewCount||integer(int32)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"categoryId": 0,
		"categoryName": "",
		"contactWay": "",
		"createTime": "",
		"description": "",
		"favoriteCount": 0,
		"id": 0,
		"images": [],
		"isFavorite": true,
		"originalPrice": 0,
		"price": 0,
		"productCondition": 0,
		"productConditionDesc": "",
		"quantity": 0,
		"sellerInfo": {
			"avatar": "",
			"creditScore": 0,
			"id": 0,
			"publishCount": 0,
			"soldCount": 0,
			"username": ""
		},
		"status": 0,
		"statusDesc": "",
		"title": "",
		"updateTime": "",
		"viewCount": 0
	},
	"msg": ""
}
```


## 获取热门商品


**接口地址**:`/product/hot`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 分页获取所有商品


**接口地址**:`/product/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|categoryId||query|false|integer(int32)||
|keyword||query|false|string||
|maxPrice||query|false|number||
|minPrice||query|false|number||
|page||query|false|integer(int32)||
|pageSize||query|false|integer(int32)||
|productCondition||query|false|integer(int32)||
|sort||query|false|string||
|status||query|false|integer(int32)||
|title||query|false|string||
|userId||query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«PageResult»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageResult|PageResult|
|&emsp;&emsp;records||array|object|
|&emsp;&emsp;total||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [],
		"total": 0
	},
	"msg": ""
}
```


## 分页获取当前用户所有商品


**接口地址**:`/product/my-products`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|categoryId||query|false|integer(int32)||
|keyword||query|false|string||
|maxPrice||query|false|number||
|minPrice||query|false|number||
|page||query|false|integer(int32)||
|pageSize||query|false|integer(int32)||
|productCondition||query|false|integer(int32)||
|sort||query|false|string||
|status||query|false|integer(int32)||
|title||query|false|string||
|userId||query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«PageResult»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageResult|PageResult|
|&emsp;&emsp;records||array|object|
|&emsp;&emsp;total||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [],
		"total": 0
	},
	"msg": ""
}
```


## 删除商品


**接口地址**:`/product/{id}`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 下架商品


**接口地址**:`/product/{id}/off`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


# 收藏接口


## 添加收藏


**接口地址**:`/favorite/add`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|createTime||query|false|string(date-time)||
|id||query|false|integer(int64)||
|productId||query|false|integer(int64)||
|userId||query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 取消收藏


**接口地址**:`/favorite/cancel`


**请求方式**:`DELETE`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|createTime||query|false|string(date-time)||
|id||query|false|integer(int64)||
|productId||query|false|integer(int64)||
|userId||query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|204|No Content||
|401|Unauthorized||
|403|Forbidden||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取收藏列表


**接口地址**:`/favorite/list`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page||query|false|integer(int32)||
|pageSize||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«PageResult»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageResult|PageResult|
|&emsp;&emsp;records||array|object|
|&emsp;&emsp;total||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [],
		"total": 0
	},
	"msg": ""
}
```


# 用户模块接口


## 修改用户信息


**接口地址**:`/user`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "avatar": "",
  "college": "",
  "email": "",
  "nickname": "",
  "phone": "",
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userDTO|userDTO|body|true|UserDTO|UserDTO|
|&emsp;&emsp;avatar|||false|string||
|&emsp;&emsp;college|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;nickname|||false|string||
|&emsp;&emsp;phone|||false|string||
|&emsp;&emsp;username|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 获取当前用户详细信息


**接口地址**:`/user/info`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«User»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||User|User|
|&emsp;&emsp;avatar||string||
|&emsp;&emsp;balance||number||
|&emsp;&emsp;college||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;creditScore||integer(int32)||
|&emsp;&emsp;email||string||
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;nickname||string||
|&emsp;&emsp;password||string||
|&emsp;&emsp;phone||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;studentId||string||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;username||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"avatar": "",
		"balance": 0,
		"college": "",
		"createTime": "",
		"creditScore": 0,
		"email": "",
		"id": 0,
		"nickname": "",
		"password": "",
		"phone": "",
		"status": 0,
		"studentId": "",
		"updateTime": "",
		"username": ""
	},
	"msg": ""
}
```


## 用户登录


**接口地址**:`/user/login`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "password": "",
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userLoginDTO|员工登录时传递的数据模型|body|true|UserLoginDTO|UserLoginDTO|
|&emsp;&emsp;password|密码||false|string||
|&emsp;&emsp;username|用户名||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«UserLoginVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||UserLoginVO|UserLoginVO|
|&emsp;&emsp;id|主键值|integer(int64)||
|&emsp;&emsp;token|jwt令牌|string||
|&emsp;&emsp;userName|用户名|string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"id": 0,
		"token": "",
		"userName": ""
	},
	"msg": ""
}
```


## 修改密码


**接口地址**:`/user/password`


**请求方式**:`PUT`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "confirmPassword": "",
  "newPassword": "",
  "oldPassword": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|passwordDTO|passwordDTO|body|true|PasswordDTO|PasswordDTO|
|&emsp;&emsp;confirmPassword|||false|string||
|&emsp;&emsp;newPassword|||false|string||
|&emsp;&emsp;oldPassword|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 新用户注册


**接口地址**:`/user/register`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "college": "",
  "email": "",
  "nickname": "",
  "password": "",
  "phone": "",
  "smsCode": "",
  "studentId": "",
  "username": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|userRegisterDTO|userRegisterDTO|body|true|UserRegisterDTO|UserRegisterDTO|
|&emsp;&emsp;college|||false|string||
|&emsp;&emsp;email|||false|string||
|&emsp;&emsp;nickname|||false|string||
|&emsp;&emsp;password|||false|string||
|&emsp;&emsp;phone|||false|string||
|&emsp;&emsp;smsCode|||false|string||
|&emsp;&emsp;studentId|||false|string||
|&emsp;&emsp;username|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 根据id获取用户信息


**接口地址**:`/user/{id}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|id|id|path|true|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«User»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||User|User|
|&emsp;&emsp;avatar||string||
|&emsp;&emsp;balance||number||
|&emsp;&emsp;college||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;creditScore||integer(int32)||
|&emsp;&emsp;email||string||
|&emsp;&emsp;id||integer(int64)||
|&emsp;&emsp;nickname||string||
|&emsp;&emsp;password||string||
|&emsp;&emsp;phone||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;studentId||string||
|&emsp;&emsp;updateTime||string(date-time)||
|&emsp;&emsp;username||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"avatar": "",
		"balance": 0,
		"college": "",
		"createTime": "",
		"creditScore": 0,
		"email": "",
		"id": 0,
		"nickname": "",
		"password": "",
		"phone": "",
		"status": 0,
		"studentId": "",
		"updateTime": "",
		"username": ""
	},
	"msg": ""
}
```


# 聊天接口


## 获取聊天记录


**接口地址**:`/ws/chat/history`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page||query|false|integer(int32)||
|productId||query|false|integer(int64)||
|size||query|false|integer(int32)||
|toUserId||query|false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«PageInfo«ChatMessageVO»»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageInfo«ChatMessageVO»|PageInfo«ChatMessageVO»|
|&emsp;&emsp;endRow||integer(int64)||
|&emsp;&emsp;hasNextPage||boolean||
|&emsp;&emsp;hasPreviousPage||boolean||
|&emsp;&emsp;isFirstPage||boolean||
|&emsp;&emsp;isLastPage||boolean||
|&emsp;&emsp;list||array|ChatMessageVO|
|&emsp;&emsp;&emsp;&emsp;content||string||
|&emsp;&emsp;&emsp;&emsp;createTime||string||
|&emsp;&emsp;&emsp;&emsp;fromUserAvatar||string||
|&emsp;&emsp;&emsp;&emsp;fromUserId||integer||
|&emsp;&emsp;&emsp;&emsp;fromUserName||string||
|&emsp;&emsp;&emsp;&emsp;isRead||boolean||
|&emsp;&emsp;&emsp;&emsp;messageId||integer||
|&emsp;&emsp;&emsp;&emsp;messageType||integer||
|&emsp;&emsp;&emsp;&emsp;toUserId||integer||
|&emsp;&emsp;navigateFirstPage||integer(int32)||
|&emsp;&emsp;navigateLastPage||integer(int32)||
|&emsp;&emsp;navigatePages||integer(int32)||
|&emsp;&emsp;navigatepageNums||array|integer|
|&emsp;&emsp;nextPage||integer(int32)||
|&emsp;&emsp;pageNum||integer(int32)||
|&emsp;&emsp;pageSize||integer(int32)||
|&emsp;&emsp;pages||integer(int32)||
|&emsp;&emsp;prePage||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;startRow||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"endRow": 0,
		"hasNextPage": true,
		"hasPreviousPage": true,
		"isFirstPage": true,
		"isLastPage": true,
		"list": [
			{
				"content": "",
				"createTime": "",
				"fromUserAvatar": "",
				"fromUserId": 0,
				"fromUserName": "",
				"isRead": true,
				"messageId": 0,
				"messageType": 0,
				"toUserId": 0
			}
		],
		"navigateFirstPage": 0,
		"navigateLastPage": 0,
		"navigatePages": 0,
		"navigatepageNums": [],
		"nextPage": 0,
		"pageNum": 0,
		"pageSize": 0,
		"pages": 0,
		"prePage": 0,
		"size": 0,
		"startRow": 0,
		"total": 0
	},
	"msg": ""
}
```


## 标记消息已读


**接口地址**:`/ws/chat/read`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "fromUserId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|markReadDTO|markReadDTO|body|true|MarkReadDTO|MarkReadDTO|
|&emsp;&emsp;fromUserId|||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||object||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {},
	"msg": ""
}
```


## 发送消息


**接口地址**:`/ws/chat/send`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "content": "",
  "messageType": 0,
  "productId": 0,
  "toUserId": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|chatMessageDTO|chatMessageDTO|body|true|ChatMessageDTO|ChatMessageDTO|
|&emsp;&emsp;content|||false|string||
|&emsp;&emsp;messageType|||false|integer(int32)||
|&emsp;&emsp;productId|||false|integer(int64)||
|&emsp;&emsp;toUserId|||false|integer(int64)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«ChatResponseVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||ChatResponseVO|ChatResponseVO|
|&emsp;&emsp;code||integer(int32)||
|&emsp;&emsp;content||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;fromUserId||integer(int64)||
|&emsp;&emsp;messageId||integer(int64)||
|&emsp;&emsp;messageType||integer(int32)||
|&emsp;&emsp;productId||integer(int64)||
|&emsp;&emsp;toUserId||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"code": 0,
		"content": "",
		"createTime": "",
		"fromUserId": 0,
		"messageId": 0,
		"messageType": 0,
		"productId": 0,
		"toUserId": 0
	},
	"msg": ""
}
```


## 获取会话列表


**接口地址**:`/ws/chat/sessions`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|page||query|false|integer(int32)||
|size||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«PageInfo«ChatSessionVO»»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageInfo«ChatSessionVO»|PageInfo«ChatSessionVO»|
|&emsp;&emsp;endRow||integer(int64)||
|&emsp;&emsp;hasNextPage||boolean||
|&emsp;&emsp;hasPreviousPage||boolean||
|&emsp;&emsp;isFirstPage||boolean||
|&emsp;&emsp;isLastPage||boolean||
|&emsp;&emsp;list||array|ChatSessionVO|
|&emsp;&emsp;&emsp;&emsp;avatar||string||
|&emsp;&emsp;&emsp;&emsp;lastMessage||string||
|&emsp;&emsp;&emsp;&emsp;lastMessageTime||string||
|&emsp;&emsp;&emsp;&emsp;productId||integer||
|&emsp;&emsp;&emsp;&emsp;productImage||string||
|&emsp;&emsp;&emsp;&emsp;productTitle||string||
|&emsp;&emsp;&emsp;&emsp;unreadCount||integer||
|&emsp;&emsp;&emsp;&emsp;userId||integer||
|&emsp;&emsp;&emsp;&emsp;username||string||
|&emsp;&emsp;navigateFirstPage||integer(int32)||
|&emsp;&emsp;navigateLastPage||integer(int32)||
|&emsp;&emsp;navigatePages||integer(int32)||
|&emsp;&emsp;navigatepageNums||array|integer|
|&emsp;&emsp;nextPage||integer(int32)||
|&emsp;&emsp;pageNum||integer(int32)||
|&emsp;&emsp;pageSize||integer(int32)||
|&emsp;&emsp;pages||integer(int32)||
|&emsp;&emsp;prePage||integer(int32)||
|&emsp;&emsp;size||integer(int32)||
|&emsp;&emsp;startRow||integer(int64)||
|&emsp;&emsp;total||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"endRow": 0,
		"hasNextPage": true,
		"hasPreviousPage": true,
		"isFirstPage": true,
		"isLastPage": true,
		"list": [
			{
				"avatar": "",
				"lastMessage": "",
				"lastMessageTime": "",
				"productId": 0,
				"productImage": "",
				"productTitle": "",
				"unreadCount": 0,
				"userId": 0,
				"username": ""
			}
		],
		"navigateFirstPage": 0,
		"navigateLastPage": 0,
		"navigatePages": 0,
		"navigatepageNums": [],
		"nextPage": 0,
		"pageNum": 0,
		"pageSize": 0,
		"pages": 0,
		"prePage": 0,
		"size": 0,
		"startRow": 0,
		"total": 0
	},
	"msg": ""
}
```


# 订单接口


## 取消订单


**接口地址**:`/order/cancel`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "cancelReason": "",
  "orderNo": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderCancelDTO|orderCancelDTO|body|true|OrderCancelDTO|OrderCancelDTO|
|&emsp;&emsp;cancelReason|||false|string||
|&emsp;&emsp;orderNo|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderCancelVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderCancelVO|OrderCancelVO|
|&emsp;&emsp;cancelReason||string||
|&emsp;&emsp;cancelTime||string(date-time)||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"cancelReason": "",
		"cancelTime": "",
		"orderNo": "",
		"status": 0,
		"statusDesc": ""
	},
	"msg": ""
}
```


## 确认接单


**接口地址**:`/order/confirm`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "freight": 0,
  "orderNo": "",
  "status": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderConfirmDTO|orderConfirmDTO|body|true|OrderConfirmDTO|OrderConfirmDTO|
|&emsp;&emsp;freight|||false|number||
|&emsp;&emsp;orderNo|||false|string||
|&emsp;&emsp;status|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderConfirmVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderConfirmVO|OrderConfirmVO|
|&emsp;&emsp;confirmTime||string(date-time)||
|&emsp;&emsp;freight||number||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"confirmTime": "",
		"freight": 0,
		"orderNo": "",
		"status": 0,
		"statusDesc": ""
	},
	"msg": ""
}
```


## 创建订单


**接口地址**:`/order/create`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "buyerMessage": "",
  "productId": 0,
  "quantity": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderDTO|orderDTO|body|true|OrderDTO|OrderDTO|
|&emsp;&emsp;buyerMessage|||false|string||
|&emsp;&emsp;productId|||false|integer(int64)||
|&emsp;&emsp;quantity|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderVO|OrderVO|
|&emsp;&emsp;amount||number||
|&emsp;&emsp;buyerMessage||string||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;expireTime||string(date-time)||
|&emsp;&emsp;orderId||integer(int64)||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;productId||integer(int64)||
|&emsp;&emsp;productImage||string||
|&emsp;&emsp;productTitle||string||
|&emsp;&emsp;quantity||integer(int32)||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|&emsp;&emsp;unitPrice||number||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"amount": 0,
		"buyerMessage": "",
		"createTime": "",
		"expireTime": "",
		"orderId": 0,
		"orderNo": "",
		"productId": 0,
		"productImage": "",
		"productTitle": "",
		"quantity": 0,
		"status": 0,
		"statusDesc": "",
		"unitPrice": 0
	},
	"msg": ""
}
```


## 订单发货


**接口地址**:`/order/deliver`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "logisticsCompany": "",
  "logisticsNo": "",
  "orderNo": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderDeliverDTO|orderDeliverDTO|body|true|OrderDeliverDTO|OrderDeliverDTO|
|&emsp;&emsp;logisticsCompany|||false|string||
|&emsp;&emsp;logisticsNo|||false|string||
|&emsp;&emsp;orderNo|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderDeliverVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderDeliverVO|OrderDeliverVO|
|&emsp;&emsp;deliverTime||string(date-time)||
|&emsp;&emsp;logisticsCompany||string||
|&emsp;&emsp;logisticsNo||string||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"deliverTime": "",
		"logisticsCompany": "",
		"logisticsNo": "",
		"orderNo": "",
		"status": 0,
		"statusDesc": ""
	},
	"msg": ""
}
```


## 查询订单详情


**接口地址**:`/order/detail/{orderNo}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderNo|orderNo|path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderDetailVO»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderDetailVO|OrderDetailVO|
|&emsp;&emsp;amount||number||
|&emsp;&emsp;buyerId||integer(int64)||
|&emsp;&emsp;buyerMessage||string||
|&emsp;&emsp;buyerName||string||
|&emsp;&emsp;buyerPhone||string||
|&emsp;&emsp;cancelTime||string(date-time)||
|&emsp;&emsp;confirmTime||string(date-time)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;deliverTime||string(date-time)||
|&emsp;&emsp;expireTime||string(date-time)||
|&emsp;&emsp;freight||number||
|&emsp;&emsp;logisticsCompany||string||
|&emsp;&emsp;logisticsNo||string||
|&emsp;&emsp;orderId||integer(int64)||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;payTime||string(date-time)||
|&emsp;&emsp;productDescription||string||
|&emsp;&emsp;productId||integer(int64)||
|&emsp;&emsp;productImage||array|string|
|&emsp;&emsp;productTitle||string||
|&emsp;&emsp;quantity||integer(int32)||
|&emsp;&emsp;sellerId||integer(int64)||
|&emsp;&emsp;sellerName||string||
|&emsp;&emsp;sellerPhone||string||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|&emsp;&emsp;totalAmount||number||
|&emsp;&emsp;unitPrice||number||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"amount": 0,
		"buyerId": 0,
		"buyerMessage": "",
		"buyerName": "",
		"buyerPhone": "",
		"cancelTime": "",
		"confirmTime": "",
		"createTime": "",
		"deliverTime": "",
		"expireTime": "",
		"freight": 0,
		"logisticsCompany": "",
		"logisticsNo": "",
		"orderId": 0,
		"orderNo": "",
		"payTime": "",
		"productDescription": "",
		"productId": 0,
		"productImage": [],
		"productTitle": "",
		"quantity": 0,
		"sellerId": 0,
		"sellerName": "",
		"sellerPhone": "",
		"status": 0,
		"statusDesc": "",
		"totalAmount": 0,
		"unitPrice": 0
	},
	"msg": ""
}
```


## 分页获取当前用户所有订单


**接口地址**:`/order/pageQuery`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|currentId||query|false|integer(int64)||
|endTime||query|false|string(date-time)||
|page||query|false|integer(int32)||
|pageSize||query|false|integer(int32)||
|role||query|false|string||
|startTime||query|false|string(date-time)||
|status||query|false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«PageResult»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||PageResult|PageResult|
|&emsp;&emsp;records||array|object|
|&emsp;&emsp;total||integer(int64)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"records": [],
		"total": 0
	},
	"msg": ""
}
```


## 支付订单


**接口地址**:`/order/pay`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "orderNo": "",
  "payPassword": "",
  "payType": 0
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderPayDTO|orderPayDTO|body|true|OrderPayDTO|OrderPayDTO|
|&emsp;&emsp;orderNo|||false|string||
|&emsp;&emsp;payPassword|||false|string||
|&emsp;&emsp;payType|||false|integer(int32)||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderPayVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderPayVO|OrderPayVO|
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;payTime||string(date-time)||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|&emsp;&emsp;totalAmount||number||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"orderNo": "",
		"payTime": "",
		"status": 0,
		"statusDesc": "",
		"totalAmount": 0
	},
	"msg": ""
}
```


## 确认收货


**接口地址**:`/order/receive`


**请求方式**:`POST`


**请求数据类型**:`application/json`


**响应数据类型**:`*/*`


**接口描述**:


**请求示例**:


```javascript
{
  "orderNo": ""
}
```


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderReceiveDTO|orderReceiveDTO|body|true|OrderReceiveDTO|OrderReceiveDTO|
|&emsp;&emsp;orderNo|||false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderReceiveVO»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderReceiveVO|OrderReceiveVO|
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;receiveTime||string(date-time)||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"orderNo": "",
		"receiveTime": "",
		"status": 0,
		"statusDesc": ""
	},
	"msg": ""
}
```


## 获取订单统计信息


**接口地址**:`/order/statistics`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


暂无


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«OrderStatisticsVO»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||OrderStatisticsVO|OrderStatisticsVO|
|&emsp;&emsp;buyCompleted||integer(int32)||
|&emsp;&emsp;sellCompleted||integer(int32)||
|&emsp;&emsp;totalCostAmount||number||
|&emsp;&emsp;totalSellAmount||number||
|&emsp;&emsp;waitDeliver||integer(int32)||
|&emsp;&emsp;waitPay||integer(int32)||
|&emsp;&emsp;waitReceive||integer(int32)||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"buyCompleted": 0,
		"sellCompleted": 0,
		"totalCostAmount": 0,
		"totalSellAmount": 0,
		"waitDeliver": 0,
		"waitPay": 0,
		"waitReceive": 0
	},
	"msg": ""
}
```


## 根据orderNo查询订单


**接口地址**:`/order/{orderNo}`


**请求方式**:`GET`


**请求数据类型**:`application/x-www-form-urlencoded`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|orderNo|orderNo|path|true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«Order»|
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||Order|Order|
|&emsp;&emsp;amount||number||
|&emsp;&emsp;buyerId||integer(int64)||
|&emsp;&emsp;buyerMessage||string||
|&emsp;&emsp;cancelReason||string||
|&emsp;&emsp;cancelTime||string(date-time)||
|&emsp;&emsp;confirmTime||string(date-time)||
|&emsp;&emsp;createTime||string(date-time)||
|&emsp;&emsp;deliverTime||string(date-time)||
|&emsp;&emsp;expireTime||string(date-time)||
|&emsp;&emsp;freight||number||
|&emsp;&emsp;logisticsCompany||string||
|&emsp;&emsp;logisticsNo||string||
|&emsp;&emsp;orderId||integer(int64)||
|&emsp;&emsp;orderNo||string||
|&emsp;&emsp;payTime||string(date-time)||
|&emsp;&emsp;payType||integer(int32)||
|&emsp;&emsp;productId||integer(int64)||
|&emsp;&emsp;productImage||string||
|&emsp;&emsp;productTitle||string||
|&emsp;&emsp;quantity||integer(int32)||
|&emsp;&emsp;receiveTime||string(date-time)||
|&emsp;&emsp;sellerId||integer(int64)||
|&emsp;&emsp;status||integer(int32)||
|&emsp;&emsp;statusDesc||string||
|&emsp;&emsp;totalAmount||number||
|&emsp;&emsp;unitPrice||number||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": {
		"amount": 0,
		"buyerId": 0,
		"buyerMessage": "",
		"cancelReason": "",
		"cancelTime": "",
		"confirmTime": "",
		"createTime": "",
		"deliverTime": "",
		"expireTime": "",
		"freight": 0,
		"logisticsCompany": "",
		"logisticsNo": "",
		"orderId": 0,
		"orderNo": "",
		"payTime": "",
		"payType": 0,
		"productId": 0,
		"productImage": "",
		"productTitle": "",
		"quantity": 0,
		"receiveTime": "",
		"sellerId": 0,
		"status": 0,
		"statusDesc": "",
		"totalAmount": 0,
		"unitPrice": 0
	},
	"msg": ""
}
```


# 通用接口


## 文件上传


**接口地址**:`/user/common/upload`


**请求方式**:`POST`


**请求数据类型**:`multipart/form-data`


**响应数据类型**:`*/*`


**接口描述**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|file|file|body|false|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|Result«string»|
|201|Created||
|401|Unauthorized||
|403|Forbidden||
|404|Not Found||


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|data||string||
|msg||string||


**响应示例**:
```javascript
{
	"code": 0,
	"data": "",
	"msg": ""
}
```