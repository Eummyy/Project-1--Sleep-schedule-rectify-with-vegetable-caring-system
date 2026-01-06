import serial as se
import socket
import time
import cv2
import json
import struct

host='0.0.0.0'
port=8888

try:
	ser=se.Serial('/dev/ttyACM0',9600,timeout=1000)

	socket_server=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
	socket_server.bind((host,port))

	socket_server.listen(1)

	print('Launching the server,waiting for the client')
	conn,adress=socket_server.accept()
	print('client is connected')

	
	cap=cv2.VideoCapture(0)
except:
	conn.close()
	socket_server.close()
	

	


if(ser.isOpen()==False):
    
    ser.open()
    time.sleep(2)

last_cap_time=time.time()

try:
	while True:

		message=str(ser.readline())
		
		if time.time()-last_cap_time>10:
			
			ret,frame=cap.read()
			frame=cv2.resize(frame,(320,240))
			print(message)
	
			_,img_buffer=cv2.imencode('.jpg',frame)
			img_data=img_buffer.tobytes()

			header = struct.pack('>II',len(message.encode('UTF-8')),len(img_data))
			conn.sendall(header)

			conn.sendall(message.encode('UTF-8'))
			conn.sendall(img_data)

			last_cap_time=time.time()
except:
	conn.close()
	socket_server.close()
finally:
	
	conn.close()
	socket_server.close()
	ser.close()	



