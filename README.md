# couler
### Stream-based high-performance RPC framework

- couler is a simple and high-performance RPC framework.It use socket stream to transfer serilized object.   
- couler is so fast because of it is a pure RPC framework that has no extra features.   
- couler can be simple and fast.Easy to use.  


### How to use?
- See the code in test.
- Server
```java
RPCServer<RequestPojo, ResponsePojo> server = new RPCServer<RequestPojo, ResponsePojo>(9008, 3000,new MyProcessor(),protostuffSerializer);
try {
    server.open();
} catch (IOException e) {
    e.printStackTrace();
}
```
- Client
```java
SyncRPCClient<RequestPojo, ResponsePojo> client = new SyncRPCClient<RequestPojo, ResponsePojo>("localhost",9008, 5,protostuffSerializer);
client.open();
RequestPojo request = new RequestPojo();
ResponsePojo response = client.call(request,10000);
```
