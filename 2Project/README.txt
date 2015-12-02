Anthony Dario
EECS 325 Networks project 2

distMeasurement.py
    
    About:
        This is a tool to find out the RTT and number of hops a packet takes 
    from your computer to a destination. This tool will read addresses or 
    hostnames from the file 'targets.txt' and print out the desired information
    to the command line and to a .csv file named 'times.csv'.

    Instructions:
        To run the program use the command 'python distMeasurement.py'

    Sample output:

            calling on address: google.ru
            resolving hostname...
            216.58.216.67 : ord30s21-in-f3.1e100.net
                number of hops: 9
                Time: 78.5820484161ms

geoDistance.py

    About:
        This is a tool to calculate the geographical distance from your computer
    to the destination addresses. The script reads from 'targets.txt' and 
    prints the geographical distance for each address in that file. The 
    distances are also outputted to a .csv file 'distances.csv'

    Instructions:
        To run the program use the comman 'python geoDistance.py'

    Sample output:
        
        199.181.133.61
            latitude: 33.7866
            longitude: -71.0843
            distance: 875.135034861km
