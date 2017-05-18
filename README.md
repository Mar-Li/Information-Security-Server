# Manual

### Pre-requirement

1. Update your jre to latest version 1.8.x, for we use Java GUI and swing library is different from previous versions.
2. Download [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
   Extract the jar files from the zip and save them in _${JAVA_HOME}/jre/lib/security/_, it will cover original files. **This is critical, for default Java Cryptography policy only support 128-bit AES key.** While in our project, 256-bit session key in used for security concern. **Without this step, you will get a _java.security.InvalidKeyException_**.
3. If your OS is Windows, you need to modify path format marked by _TODO_.
4. The recommend IDE is _IntelliJ IDEA_ and _Eclipse_.


### Run the program

1. Run _src/server/Server.java_ **first**, default port is **2333**, and you can change it in the java file.
2. Run _src/client/Client.java_ and two threads will be run, then you should create 2 clients in the pop frames. Our default client port is **3000** & **3001**, it is not suggested to modify it :)
3. Register two clients with **different usernames** separately, the password is optional.
4. After two clients are registered successfully, you can befriend B with A and send message or file between each other.  




# Information Security Project Document

2017.5

马叶舟 14302010052

李逢双 13302010002

## Security solution

In our project, messaging and file sending are encrypted by AES. All the rest are encrypted by RSA.

### Algrithm and Key size

- Asymmetric key: RSA 2048 bits
- Symmetric key: AES 256 bits

### Keystore

Client holds:

(own keypair is stored in java.security.KeyStore, access password is set in registration, no password is acceptable. )

- Server's public key
- It's own private key and public key
- Friends' public keys
- Temp AES session key in IM or file transfer

Server holds:

(all stored in file, public key in X509EncodedKeySpec format, private key in PKCS8EncodedKeySpec format.)
- Server's public key and private key
- All users' public key


### Key usage

#### First contact

In order to encrypt user's private key returned by server in register service, the client first generates a temporary RSA key pair and sends its public key in the register request (the request is encrypted by server's public key). Then the server encrypts the user's private key by this public key to ensure the confidentiality.

#### Message between client and server

All the message are encrypted with RSA key. All the messages are small in size so the efficiency is not bad.

#### Message between two clients

The messages are encrypted by AES. Some overheads encrypted by RSA are added to convey the metadata and ensure confidentiality and authenticity.

### Message format

**All the messages** in our project are fully encrypted along with a signature in the following format:

 ![Screen Shot 2017-05-17 at 9.29.25 PM](https://cloud.githubusercontent.com/assets/6532225/26182030/8089c370-3ba7-11e7-903d-bf2ea96f968c.png)

- Header length:
  - Record the length of the header. 
  - Help separate Header and Body.
  - 2048 bit in length because the RSA key is 2048 bit. Fixed length.
  - *Encrypted by receiver’s public key*.
- Header:
  - Contain key-value pairs, similar to HTTP header.
  - Specify some metadata of the request. So the size of Header is not fixed.
    - Service: addFriend
    - Username: Alice
    - Friend: Bob
    - etc.
  - Header items are parsed to string `"<key>:<value>\n<key>:<value>\n<key>:<value>"` and then *encrypted by receiver's public key*.
- Body
  - Body contains the main message.
    - Temp RSA public key when client first contact the server
    - IM messages
    - File data
  - *Encrypted by receiver's public key or shared symmetric key*.
- Signature
  - Signature is SHA-1 hash of all the former bytes 
  - Then it’s encrypted by sender’s private key
  - 2048 bit in length, fixed length.

### Goals

For all the messages, we ensured the following:

#### Confidentiality

Every part of the messages are encrypted. Without the private key and symmetric key, hackers are only able to validate the signature with the public key, which is meanless.

#### Integrity

Ensured by signature

#### Authentication

Ensured by signature

#### Efficiency

For all the messages encrypted by RSA, they are small in size.

- Header length block. 2048 bit.
- Header block. All the headers are small. Currently all of them are 2048 bit.
- Body block. Used for key exchange with RSA or encrypted by AES.
- Signature block. 2048 bit.

Thus, we ensured our encryption efficiency.

## Implementation details

### GUI code

Implemented by Java Swing. See code under src/GUI.

### Utility code

- util/KeyGenerator.java
  - Generate, save and load RSA and AES keys.
- util/EncryptionUtils.java
  - Encrypt and decrypt messages with RSA or AES key.
- util/CommonUtils.java
  - Supporting functions to parse data among byte array, String and Object.
- Message Wrapper
  - Assembly and disassembly messages in the message format mentioned before.
  - util/message/MessageWrapper.java
    - Encrypt and assembly header and body, adding header length block and signature. Return a byte array for socket to send.
    - Receive a byte array, decrypt and disassembly the messages to extract header and body.
  - utils/message/MessageHeader.java
    - Code for the header
  - utils/message/MessageHeaderItem.java
    - Code for items in header

### Service code

#### 1. Registeration

Client generate temp RSA keypair to ensure confidentiality of server's response 

Client sends:

- Header, encrypted by server's public key
  - Service: register
  - Username: [username]
  - Port: [port] // User's listening port for IM and File sending. 
- Body, encrypted by server's public key
  - temp public key

Server generate a keypair for the new user and replies:

- Header, encrypted by temp public key
  - Service: register
- Body, encrypted by temp public key
  - user's true private key and public key

#### 2. Friending

Client A sends to server:

- Header, encrypted by server's public key
  - Service: addFriend
  - Username: A
  - Friend: B
- Body
  - nothing

Server forwards the friending request to B:

- Header, encrypted by B's public key
  - Service: friendRequest
  - Username: A
- Body, encrypted by B's public key
  - A's information, including public key, ip and server port.

Client B replies server:

- Header, encrypted by server's public key
  - Response: Accepted/Rejected
- Body
  - nothing

Server replies client A:

- Header, encrypted by A's public key
  - Service: addFriend
  - Response: accepted/rejected
- Body, encrypted by A's public key
  - B's public key, ip and server port.

#### 3. Messaging

##### Build connection between A & B

Client A sends to Client B:  
(B's IP and listening port is provided by server in BeFriend process)

- Header, encrypted by B's public key
  - Service: InitChat
  - Username: A
- Body, encrypted by B's public key
  - AES session key generated by A

Client B replies:

- Header, encrypted by A's public key
  - Service: ConfirmChat
  - Username: B
  - Status: 200
- Body, encrypted by session key provided by A
  - Confirm  

_B turn on messaging A window_

Client A authenticate B and checks whether B has received correct session key:

- Header.Status == 200
- Decrypted body message == Confirm

_A turn on messaging B window_

##### Start IM

Client A sends to B:

- Header, encrypted by B's public key
  - Service: Chat
  - Username: A
- Body, encrypted by session key
  - _Any language is allowed_

#### 4. File sending

**After connection is built**

Client A sends to B:

- Header, encrypted by B's public key
  - Service: File
  - Username: A
- Body, encrypted by session key
  - File chosen through JFileChooser


### Error handling

If server can identify the sender's identity, the server will return an encrypted message when error occurs.

Format:

- Header, encrypted by sender's public key
  - Status: Error
  - Error: [Error message]
  - ErrorType: Exception class, like DuplicatedUserException
- Body
  - nothing

If server can't identify the sender's identity, the server will return a byte with value 0. This will not make the protocol ambiguous because all the messages in our format are much longer than one byte. The server won't reply the sender with plaintext, because we want to ensure that nobody is able to know anything about the communication.