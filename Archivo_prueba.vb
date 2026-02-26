Imports System
Imports System.IO
	
	'Programa avanzado de ejemplo para análisis léxico
Module ProyectoVB
    Sub Main()
        'Declaración de variables
        Dim numero1 As Integer = 7
        Dim numero2 As Integer = 12
        Dim texto As String = "Resultado: "
        Dim esMayor As Boolean

        'Suma y comparación
        Dim suma As Integer = numero1 + numero2
        esMayor = suma > 15

        'Condición anidada
        If esMayor Then
            Console.WriteLine(texto & "La suma es mayor que 15")
        ElseIf suma = 15 Then
            Console.WriteLine(texto & "La suma es exactamente 15")
        Else
            Console.WriteLine(texto & "La suma es menor que 15")
        End If

       'Llamada a función con parámetros
        	
        producto = Multiplicar(numero1, numero2)
        Console.WriteLine("El producto es: " & producto)

        ' Uso de operadores lógicos
        If esMayor And producto > 50 Then
            Console.WriteLine("La suma es mayor y el producto supera 50")
        End If
    End Sub

    ' Función para multiplicar dos números
    Function Multiplicar(a As Double, b As Double) As Double
        Return a * b
    End Function
End Module