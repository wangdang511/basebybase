#Colum McClay 2 Dec 2015

#Script to import codon tables from http://www.kazusa.or.jp/codon/cgi-bin/showcodon.cgi?species=9606&aa=1&style=N
#to java syntax to be pasted into the CodonTable class in Codehop. Values to be hard coded in java, this script
#simply facilitates this process.

#to use, copy+paste the table in the correct format to a text file. set the 'path' variable below to your file.
#Remove new lines from pasted codon tables, and whitespace inside brackets before running this script
#(parser uses white space to tokenize) See tables for example.

#entries in the table are formatted like: [triplet] [amino acid] [fraction] [frequency: per thousand] ([number]) 
#we are only interested in [triplet][amino acid][fraction]

#output will in file 'outjava.txt'. 

#Paste this output into a new method setXxxx() in CodonTable.java  where Xxxx  is the name of the codontable
#If adding a new table, do no forget to add it to the dropdown in the select panel.

from collections import defaultdict

out=''
data_dict = defaultdict(list)

def main():
	path='C:/development/base-by-base/src/main/java/ca/virology/baseByBase/gui/CodeHop/CodonTables/HomoSapien.txt'
	rawVals = open(path).read().split()
	take=0
	values=''
	aa=''
	for val in rawVals:
		take+=1
		if take<=3: #we are looking at one of the appropriate fields
			if val.startswith('link='):
				break
			values+=val+' '
			if take==2: #this is the AA field, remember it
				aa=val
			if take==3: #we have the last field, we can add it to the dictionary, where the key value is the AA
				addToDict(values.replace('U','T'),aa)

		else:
			if take==5:
				values=''
				take=0
	DictToJava()			


def DictToJava():
	f=open('outjava.txt','w')
	diffAA=True
	for entry in data_dict:
		diffAA=True
		for item in data_dict[entry]:
			if diffAA:
				f.write('lookupVal=getLookupVal(\''+item[4]+'\');\n')
			f.write('codonArray[lookupVal].add(new CodonTableEntry(\"'+item[0:3]+'\",'+item[6:10]+',\"'+item[4]+'\"));\n')
			diffAA=False
	f.close()

def addToDict(data,aa):
	data_dict[aa].append(data)






if __name__ == '__main__':
	main()