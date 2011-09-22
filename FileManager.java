import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class FileManager {

	
	public void split(File inputFile, int noPartitions)
	{

		if(inputFile.length()>Integer.MAX_VALUE)
			return;
		
		FileInputStream fInputStream;
		FileOutputStream partition;
		String newName;
		int fileSize = (int) inputFile.length();
		int partitionSize  =  fileSize/noPartitions  ;
		int partitionNumber = 0, read = 0, readLength = partitionSize;
		
		byte[] byteChunk;
		/*byte[][] byteChunk;
		byteChunk=new byte[noPartitions][];
		
		for(int i=0; i<noPartitions; i++)
		{
			if(i<noPartitions-1)
				byteChunk[i]=new byte[partitionSize];
			else
				byteChunk[i]=new byte[partitionSize + fileSize%partitionSize];
		}
		*/
		try 
		{
		    fInputStream = new FileInputStream(inputFile);
		    int index=0;
		    while (fileSize > 0) 
		    {
		        if (fileSize <= 2*partitionSize) 
		        {
		            readLength = fileSize;
		        }
		        byteChunk = new byte[readLength];
		        read = fInputStream.read(byteChunk, 0, readLength);
		        System.out.println("checkpoint ind " + index + " ReadSize " + read);
		        fileSize -= read;
		        assert(read==byteChunk.length);
		        partitionNumber++;
		        newName = inputFile.getName() + ".part" + Integer.toString(partitionNumber - 1);
		        partition = new FileOutputStream(new File(newName));
		        partition.write(byteChunk);
		        partition.flush();
		        partition.close();
		        byteChunk = null;
		        partition = null;
		        index++;
		    }
		   
		    fInputStream.close();
			 fInputStream = null;
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		 //return byteChunk ;
		
	}
	
	
	public byte[][] split1(File inputFile, int noPartitions)
	{

		if(inputFile.length()>Integer.MAX_VALUE)
			return null;
		
		FileInputStream fInputStream;
		FileOutputStream partition;
		String newName;
		int fileSize = (int) inputFile.length();
		int partitionSize  =  fileSize/noPartitions  ;
		int partitionNumber = 0, read = 0, readLength = partitionSize;
		int index=0, offset=0;
		
		byte[][] byteChunk=new byte[noPartitions][];
		 System.out.println("Partition size "+ partitionSize + " Remainder: " + partitionSize+fileSize%partitionSize);
		
		for(int i=0; i<noPartitions; i++)
		{
			if(i<noPartitions-1)
				byteChunk[i]=new byte[partitionSize];
			else
				byteChunk[i]=new byte[partitionSize + fileSize%partitionSize];
		}
		
		try 
		{
		    fInputStream = new FileInputStream(inputFile);
		    
		    while (fileSize > 0) 
		    {
		        if (fileSize < 2*partitionSize) 
		        {
		            readLength = fileSize;
		        }
		        //System.out.println("Offset "+ offset +" readlength " + readLength + " Ind:" +index + "Read: " + read);
		        // byteChunk = new byte[readLength];
		        read = fInputStream.read(byteChunk[index], 0, readLength);
		        System.out.println("checkpoint ind " + index + " ReadSize " + read);
		        fileSize -= read;
		        offset+=read;
		       // assert(read==byteChunk[index].length);
		        partitionNumber++;
		        newName = inputFile.getName() + ".part" + Integer.toString(partitionNumber - 1);
		        partition = new FileOutputStream(new File(newName));
		        partition.write(byteChunk[index]);
		        partition.flush();
		        partition.close();
		       // byteChunk = null;
		        partition = null;
		        index++;
		    }
		   
		    fInputStream.close();
			 fInputStream = null;
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		 return byteChunk ;
		
	}
	
	public void reconstruct()
	{
		
	}
	
	public static void main(String[] args) 
	{
		String fileName="/Users/navaneeth/Desktop/Alegra.mp4";
		File inFile= new File(fileName); 
		FileManager instance=new FileManager(); 
		//System.out.println("checkpoint 1");
		//instance.split(inFile, Parameters.TotalPartitionNo);
		
		byte[][] k=instance.split1(inFile, Parameters.TotalPartitionNo);
		System.out.println("checkpoint END " + k[3].length);
		String baseDirectoryName="Node", drName;
		
		for(int i=0; i<Parameters.TotalPartitionNo; i++)
		{
			drName=baseDirectoryName+i;
		  	(new File(drName)).mkdir();
		  	//store the file
		}
		
	}
		

	

}
