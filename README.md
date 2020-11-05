# couler
### Stream-based high-performance RPC framework

- couler is a simple and high-performance RPC framework.It use socket stream to transfer serilized object depends on protostuff.   
- couler is so fast because of it is a pure RPC framework that has no extra features.   
- Based on protostuff, couler can be simple and fast.Easy to use.  


### How to use?
- See the code in test.
- Server
```java
RPCServer server = new RPCServer(8000,RequestPojo.class,ResponsePojo.class,200,new MyProcessor());
try {
    server.open();
} catch (IOException e) {
    e.printStackTrace();
}
```
- Client
```java
RPCClient<RequestPojo,ResponsePojo> client = new RPCClient<RequestPojo,ResponsePojo>("127.0.0.1",9004,RequestPojo.class,ResponsePojo.class,5);
client.open();
Message message = new Message();
Message result = client.call(message,10000).getName();
```
