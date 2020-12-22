'''
	INGRESAR POR EL TECLADO:
		envio n a b funcion
			n = Número de intervalos 
			a = limite inferior 
			b = limite superior 
			funcion = De la forma 1*x^3-4*x^2+4*x^1-10*x^0
'''

import socket
import threading
import decimal
from decimal import Decimal, getcontext
import math
import sys
from time import time

#HILO QUE RECIVE LA RESPUESTA DEL CLIENTE
class Hilo(threading.Thread):
	def __init__(self, conexion, addr, id, lock):
		threading.Thread.__init__(self, target=Hilo.run)
		self.id = nCliente
		self.conexion = conexion
		self.addr = addr
							
	def run(self):
		while True:
			recibe = self.conexion.recv(1024).decode("UTF8")
			if recibe.lower() == "salir".lower():
				print("Cliente {} se desconecto".format(id))
				break
			if recibe!="":
				print("Recibo de cliente {0}: {1}".format(self.id, recibe))
				#synchronized
				lock.acquire()  #candado
				recibeDelCliente(recibe, self.id) 
				lock.release()  #libera candado

#SOLO PERMITE LA ENTRADA DE UN HILO A LA VEZ
def recibeDelCliente(recibe, id):
	global contarCliente, respuestaCliente, nCliente, a, b, n, tiempo_inicial, tiempo_final
	contarCliente += 1
	suma = Decimal('0')

	getcontext().prec = 200 #indica la precision
	respuestaCliente.append(Decimal(recibe)) #almacena el valor enviado por el cliente

	#Si se recibio mensaje de todos los clientes
	#Suma la espuesta enviada por los clientes
	if nCliente == contarCliente:
		for r in respuestaCliente:
			suma += r

		tiempo_final = time()
		print("Tiempo estimado: {0}".format(tiempo_final-tiempo_inicial))
		print("Respuesta: {0}".format(suma))


		#restablece los valores 
		contarCliente = 0
		for i in range(len(respuestaCliente)):
			respuestaCliente[i] = Decimal("0")
		tiempo_inicial = 0
		tiempo_final = 0


#----------------------------------------------------------------
#----------------------------------------------------------------
#HILO DEL TECLADO
def teclado():
	mensaje = input()
	while mensaje.lower()!="SALIR".lower():
		enviaServidor(mensaje)
		mensaje = input()
	cerrarConexion()

#envio: palabra clave para enviar mensaje al cliente
#mensaje escrito en el teclado: envio n a b funcion 
#mensaje enviado al cliente: min max n a b funcion
def enviaServidor(mensaje):
	global conexiones, n, a, b, funcionEnviada, tiempo_inicial
	if mensaje.find("envio")==0: 
		tiempo_inicial = time()
		lista = mensaje.split(" ")
		n = float(lista[1])
		a = lista[2]
		b = lista[3]
		funcion = lista[4]

		prefijos = indices_intervalo(n, len(conexiones))
		funcionEnviada = transformarFuncion(funcion)
		for i in range(len(conexiones)):
			enviar = "envio {0} {1} {2} {3} {4} {5} \n".format(prefijos[i], prefijos[i+1]-1, n, a, b, funcionEnviada)
			conexiones[i].send(enviar.encode("UTF8"))	
			print("enviando: {0} a cliente : {1}".format(enviar, i+1))

#arreglo que almacena los primeros elementos de cada intervalo
#Ejm: para n=100 y 2 hilos => [1, 51, 101] 
#Con esto se puede obtener [1, 50] [51, 100]
def indices_intervalo(intervalo,nClientes):
    cociente = int(intervalo / nClientes)
    residuo = intervalo % nClientes
    r = []
    for i in range(nClientes):
        item = cociente
        if(residuo>0):
            item=item+1
            residuo=residuo-1
        r.append(item)
    prefijos=[]
    prefijos.append(0)#cambio
    for i in range(1,len(r)+1):
        fijo = prefijos[i-1] + r[i-1]
        prefijos.append(fijo)
    return prefijos

#Cambia a la funcion de 1*x^2+4*x^1+1*x^0 => +1,2;+4,1;+1,0
def transformarFuncion(funcion):
	aux01 = funcion.replace("+",";+").replace("-",";-").replace("*x^",",")
	if aux01[0]!="+" and aux01[0]!="-":
		aux01 = "+" + aux01
	return aux01

#envia la palabra salir para que los clientes finalizen la conexion
def cerrarConexion():
	global conexiones
	for c in conexiones:
		c.send("salir".encode("UTF8"))
	print("finalizo servicio")
	sys.exit(0)

#----------------------------------------------------------------
#----------------------------------------------------------------

#Almacena todas las conexiones abiertas entre el servidor y cliente
#Se usa para enviar mensaje a todos los clientes
conexiones = [] 

nCliente = 0
respuestaCliente = []
contarCliente = 0

#pares del mensaje
# envio n a b funcionEnviada
funcionEnviada = ""
n = 0
a = 0
b = 0

#controla el acceso al recurso
lock = threading.Lock() 

tiempo_inicial = 0
tiempo_final = 0

if __name__ == '__main__':

	mi_socket = socket.socket()
	mi_socket.bind( ('192.168.1.5', 8000))
	mi_socket.listen(5) #número de conexiones simultaneas

	#Hilo del teclado
	hilo1 = threading.Thread(target=teclado)
	hilo1.start()

	#Genera las conexiones con los clientes
	while True:
		conexion, addr = mi_socket.accept()
		conexiones.append(conexion)
		nCliente+=1
		hilo = Hilo(conexion,addr, nCliente, lock)
		hilo.start()
		print ("Cliente {0} conectado".format(nCliente))

	
