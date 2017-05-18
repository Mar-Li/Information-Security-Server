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

- Server's public key
- It's own private key and public key
- Friends' public keys
- Temp AES key

Server holds:

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
  - Signature is md5 hash of all the former bytes 
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

Client sends:

- Header, encrypted by server's public key
  - Service: register
  - Username: [username]
  - Port: [port] // User's server port. Used by IM and File sending. 
- Body, encrypted by server's public key
  - temp public key

Server replies:

- Header, encrypted by temp public key
  - Service: register
- Body, encrypted by temp public key
  - user's private key and public key

#### 2. Friending

Client A sends:

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
- Body
  - A's information, including public key, ip and server port.

Client B replies server:

- Header, encrypted by temp public key
  - Response: accepted/rejected
- Body
  - nothing

Server replies client A:

- Header, encrypted by A's public key
  - Service: addFriend
  - Response: accepted/rejected
- Body
  - nothing

#### 3. Messaging

#### 4. File sending



