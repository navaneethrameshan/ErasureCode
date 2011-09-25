import java.io.File;
import java.util.*;

public class Regeneration {

	int regen_from_nodes[];
	int failed_node;
	int no_nodes_to_regenerate;
	static boolean reconstruct=false;

	ArrayList<Integer[]> gen_vector;
	ArrayList<Integer[]> index_gen_vector;
	ArrayList<Integer[]> intermediate_gen_vector;
	ArrayList<Integer[]> index_intermediate_gen_vector;
	ArrayList<Integer[]> to_fetch;
	ArrayList <Integer> mem_bit1;
	ArrayList <Integer> mem_bit0;
	ArrayList<Integer> fallback_fetch;


	public Regeneration() {
		// TODO Auto-generated constructor stub
		mem_bit1 = new ArrayList<Integer>();
		mem_bit0 = new ArrayList<Integer>();	
		gen_vector = new ArrayList<Integer[]>();
		index_gen_vector = new ArrayList<Integer[]>();
		intermediate_gen_vector = new ArrayList<Integer[]>();
		index_intermediate_gen_vector = new ArrayList<Integer[]>();
		to_fetch = new ArrayList<Integer[]>();
		fallback_fetch = new ArrayList<Integer>();

	}

	public void clean(){
		mem_bit1.clear();
		mem_bit0.clear();
		gen_vector.clear();
		index_gen_vector.clear();
		intermediate_gen_vector.clear();
		index_intermediate_gen_vector.clear();
		to_fetch.clear();
		fallback_fetch.clear();
		reconstruct = false;
	}
	public boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	public void input(){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Do you want to \n 1. Reconstruct whole file \n 2. Regenerate data on failed node. \n Press 1 or 2. ");
		int choice = scanner.nextInt();
		if(choice == 2){
			System.out.println("Enter the node number to fail");
			failed_node = scanner.nextInt();
			deleteDir(new File("Node"+failed_node));
		}
		else{
			reconstruct = true;
			return;
		}

		System.out.println("How many nodes do you want to regenerate from? ");
		no_nodes_to_regenerate = scanner.nextInt();
		regen_from_nodes= new int [no_nodes_to_regenerate];
		for(int i=0; i<no_nodes_to_regenerate;i++){
			System.out.println(i+1+". Enter Node: ");
			regen_from_nodes[i] = scanner.nextInt();
			while(regen_from_nodes[i] == failed_node || regen_from_nodes[i]>4){
				System.out.println("Input rejected!! Please enter another node.");
				regen_from_nodes[i] = scanner.nextInt();
			}
		}	
		identifyParts();
		System.out.println("!!DONE!!");
	}

	public void identifyParts(){

		for(int i=0; i<(BasisVector.PARTSPERNODE); i++  ){
			for(int j= 0; j< BasisVector.VECTORSIZE; j++ ){	
				if (BasisVector.list[failed_node][i][j] == 1){
					System.out.println("BitValue" +BasisVector.list[failed_node][i][j] + " [Failed node "+ failed_node +" ] Vector: " +i+ " at pos: " + (int)(j));
					printBasisVector(failed_node, i);
					findMatchFromGeneratingNodes(i,j);
					boolean match = startXORComparison(i);

					if(match){
						//Flush memory, XORed value stored in gen_vector #IGNORE
					}
					else{
						if(!identifyIntermediate(i,j)){
							System.out.println();
							System.out.println("FALLBACK MECHANISM");
							boolean flag = false;
							for(int k =0; k<no_nodes_to_regenerate;k++ ){
								for(int l=0; l<BasisVector.PARTSPERNODE; l++){
									Integer[] source = getSource(regen_from_nodes[k],l);
									ArrayList<Integer[]> list = generateListFallback(regen_from_nodes[k],l);
									if(fallBackIdentifyIntermediate(i, j, list, source)){
										//Add source to the fall back list///
										int nodepart = returnNodePartFromVector(source);
										fallback_fetch.add(nodepart);
										Integer[] final_fetch= convertIntegers(fallback_fetch);
										to_fetch.add(final_fetch);
										System.out.println("!!!!!Fall Back Success!!!!!!");
										flag= true;
										break;
										///////////////////////////////////
									}
									System.out.println("((New Source))");
								}
								if(flag)
									break;
							}
						}
						else{
							System.out.println("!!!!!Intermediate match!!!");
						}
					}
					gen_vector.clear();
					intermediate_gen_vector.clear();
					mem_bit0.clear();
					mem_bit1.clear();
					index_gen_vector.clear();
					index_intermediate_gen_vector.clear();
					
					fallback_fetch.clear();
					break;
				}
			}

			System.out.println("-----------------------");
		}

	}

	
	public static Integer[] convertIntegers(ArrayList<Integer> integers)
	{
	    Integer[] ret = new Integer[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
	}


	public ArrayList<Integer[]> generateListFallback(int node, int part){
		//System.out.println("LIST IS:: ");
		ArrayList<Integer[]> list = new ArrayList<Integer[]>();
		for( int i = 0; i<no_nodes_to_regenerate; i++){
			for(int j=0; j<BasisVector.PARTSPERNODE;j++){
				if(regen_from_nodes[i]!=node || j!=part){ //Avoid putting the first Vector to list. It remains as source
					//System.out.print(" Inside ");
					Integer temp[] = new Integer[BasisVector.VECTORSIZE];
					for(int k=0; k<BasisVector.VECTORSIZE;k++){
						temp[k] = Integer.valueOf(BasisVector.list[regen_from_nodes[i]][j][k]);
						//	System.out.print(temp[k]);
					}
					list.add(temp);
				}
				//	System.out.print("<--->");
			}
		}

		return list;
	}

	public Integer[] getSource(int node, int part){
		Integer[] temp = new Integer[BasisVector.VECTORSIZE];  
		for(int k=0; k<BasisVector.VECTORSIZE;k++){
			temp[k] = Integer.valueOf(BasisVector.list[node][part][k]);
		}
		return temp;
	}


	public boolean identifyIntermediate (int failednode_part, int failednode_firstbit_position){
		for (int k=failednode_firstbit_position; k<BasisVector.VECTORSIZE; k++){
			System.out.println();
			System.out.println("Beginning Intermediate Calculation");
			int position = identifyNextBitPosition(failednode_part,k);
			k= position;
			if(position == -1){
				System.out.println("No Solution by Optimistic approach");
				break;
			}
			for (int i=0; i<gen_vector.size(); i++){		
				if(gen_vector.get(i)[position] == 0){
					//find vectors with value 1 at position.
					if(findBasisVectorGivenPosition(1, position, gen_vector.get(i),failednode_part, i)){
						return true;
					}
				}

				else{
					//find vectors with value 0 at position.
					if(findBasisVectorGivenPosition(0, position, gen_vector.get(i),failednode_part, i)){
						return true;
					}
				}
			}
			//Control here means, couldn't find in the first intermediate level.
			//copy intermediate_gen_vector to gen_vector and flush gen_vector.
			//copy index_intermediate_gen_vector to index_gen_vector and flush index_gen_vector

			copyArrayList(gen_vector, intermediate_gen_vector);
			copyArrayList(index_gen_vector, index_intermediate_gen_vector);


		}

		return false;


	}

	public boolean fallBackIdentifyIntermediate(int failednode_part, int failednode_firstbit_position, ArrayList<Integer[]> list,Integer[] source ){
		int node,part; 
		if(list.size() == 0){
			System.out.println("");
			return false;
		}
		for(int i=0; i<list.size();i++){
			System.out.println();
			System.out.print("XORing: ");
			Integer[] result = XORForFallBack(list.get(i), source);
			/////TO print///////////////////////////
			node = returnNodePartFromVector(list.get(i))/10;
			 part= returnNodePartFromVector(list.get(i))%10;
			printBasisVector( node,part);
			System.out.print(" with ");
			for (int k=0; k<BasisVector.VECTORSIZE;k++){
				System.out.print(source[k]);
			}
			System.out.print(" ---> ");
			for (int k=0; k<BasisVector.VECTORSIZE;k++){
				System.out.print(result[k]);
			}
			///////////////////////////////////////////////
			
			if(compareEqualityIntermediate(failednode_part, result)){
				//System.out.println("!!MATCH FOUND HERE!!");
				int nodepart = returnNodePartFromVector(list.get(i));
				//printBasisVector(nodepart/10, nodepart%10);
				fallback_fetch.add(nodepart);
				return true;
			}
			
			//get all elements from the list except the element used to compute the XOR
			ArrayList<Integer[]> newlist= getElementsExcept(list, i);

			if(fallBackIdentifyIntermediate(failednode_part, failednode_firstbit_position, newlist, result)){
				int nodepart = returnNodePartFromVector(list.get(i));
				//printBasisVector(nodepart/10, nodepart%10);
				fallback_fetch.add(nodepart);
				return true;
			}
		}

		return false;
	}

	public Integer returnNodePartFromVector(Integer[] vector){
		boolean flag= true;
		Integer value;
		for(int i =0; i< BasisVector.TOTALNODES; i++){
			for(int j=0; j<BasisVector.PARTSPERNODE; j++){
				for(int k =0; k<BasisVector.VECTORSIZE;k++){
					if (vector[k]!= BasisVector.list[i][j][k]){
						flag =false;
					}
				}
				if(flag){
					value = i*10+j;
					return value;
				}
				flag = true;
			}
		}
		return -1;
	}

	public ArrayList<Integer[]> getElementsExcept(ArrayList<Integer[]> list, int position){
		ArrayList<Integer[]> newlist = new ArrayList<Integer[]>();
		for (int i=0;i<list.size();i++){
			if(i!=position){
				newlist.add(list.get(i));
			}
		}
		return newlist;
	}

	public Integer[] XORForFallBack(Integer[] vector1, Integer[] vector2){
		Integer[] result = new Integer[BasisVector.VECTORSIZE];
		for(int i=0; i<vector1.length;i++){
			result[i] = (vector1[i]^vector2[i]);
		}
		return result;
	}

	public void copyArrayList(ArrayList<Integer[]> genVector, ArrayList<Integer[]> interGenVector ){
		genVector.clear();
		for (int i=0; i<interGenVector.size(); i++){
			genVector.add(interGenVector.get(i));
		}
		interGenVector.clear();
	}


	public int identifyNextBitPosition(int failednode_part, int failednode_firstbit_position){
		for (int i=failednode_firstbit_position+1; i<BasisVector.VECTORSIZE; i++){
			if (BasisVector.list[failed_node][failednode_part][i] == 1){
				return i;
			}
		}
		return -1;
	}

	public boolean findBasisVectorGivenPosition(int value, int position, Integer[] generatedVector, int part, int index){
		for (int i=0; i< (no_nodes_to_regenerate); i++){
			for(int j=0; j<BasisVector.PARTSPERNODE; j++){
				for(int k =0; k< BasisVector.VECTORSIZE ;k++){
					if(BasisVector.list[regen_from_nodes[i]][j][k] == value && k==position){
						System.out.print("XORing: ");
						printBasisVector(regen_from_nodes[i], j);
						System.out.print(" with: ");
						printGeneratedVector(generatedVector);
						if(intermediateXOR(generatedVector, BasisVector.list[regen_from_nodes[i]][j],part,index, regen_from_nodes[i], j)){
							return true;
						}
						System.out.println();

					}
				}
			}
		}

		return false;
	}

	public void printGeneratedVector(Integer[] generatedVector){
		for (int i=0; i<BasisVector.VECTORSIZE; i++){
			System.out.print(generatedVector[i]);
		}
	}


	public boolean intermediateXOR (Integer[] generatedVector, int[] vector, int failed_part, int index, int node, int part){
		System.out.print(" ----> ");
		Integer[] temp = new Integer[BasisVector.VECTORSIZE];
		for (int i=0; i<BasisVector.VECTORSIZE; i++){
			temp[i]= generatedVector[i]^vector[i];
			System.out.print(temp[i]);
		}
		intermediate_gen_vector.add(temp);

		/////////////////////store intermediate index
		//copy index_gen_vector to index_intermediate_gen_vector
		Integer index_intermediate[] = new Integer[index_gen_vector.get(index).length + 1];

		for(int k=0;k<index_gen_vector.get(index).length;k++){
			index_intermediate[k] = index_gen_vector.get(index)[k];
		}
		index_intermediate[index_gen_vector.get(index).length] = node*10+part;
		index_intermediate_gen_vector.add(index_intermediate);
		//////////////////////////////////


		if(compareEqualityIntermediate(failed_part, intermediate_gen_vector.get(intermediate_gen_vector.size()-1))){
			// to get back the stored vectors in order to regenerate

			Integer[] temp1= new Integer[index_intermediate_gen_vector.get(index_intermediate_gen_vector.size()-1).length];
			for(int k=0;k<index_intermediate_gen_vector.get(index_intermediate_gen_vector.size()-1).length;k++){
				temp1[k] = index_intermediate_gen_vector.get(index_intermediate_gen_vector.size()-1)[k];
			}
			to_fetch.add(temp1);

			return true;
		}
		else
			return false;
	}


	public void printIndexArrayList(ArrayList<Integer[]> index_list ){
		System.out.println("Index printing begin");
		for (int i =0; i< index_list.size(); i++){
			for( int j: index_list.get(i)){
				printBasisVector(j/10, j%10);				
				System.out.println();
			}
		}
		System.out.println("Index printing end");

	}

	public boolean compareEqualityIntermediate(int part, Integer[] intermediateVector){
		boolean match = true;
		for (int i=0; i< BasisVector.VECTORSIZE; i++){
			if(BasisVector.list[failed_node][part][i] != intermediateVector[i] ){
				match = false;
			}
		}

		if (match){
		//	System.out.print(" !!!INTERMEDIATE MATCH SUCCESS!!! ");
			return match;
		}
		else
			return match;	
	}


	public void printBasisVector(int node, int part){
		System.out.print("");
		for(int i =0; i<BasisVector.VECTORSIZE; i++){
			System.out.print(BasisVector.list[node][part][i]);
		}
	}

	public void findMatchFromGeneratingNodes(int part_no, int position){

		for (int i=0; i< (no_nodes_to_regenerate); i++){
			for(int j=0; j<BasisVector.PARTSPERNODE; j++){
				for(int k =0; k< BasisVector.VECTORSIZE ;k++){
					if(BasisVector.list[regen_from_nodes[i]][j][k] == 1 && k==position){
						//	System.out.println("Match found in Node: "+ (int) (regen_from_nodes[i]) + " in vector no: "+ j + " at position: " + k + " for bit 1");
						rememberBit1Value(regen_from_nodes[i], j);
						//	printBasisVector(regen_from_nodes[i], j);
					}

					if(BasisVector.list[regen_from_nodes[i]][j][k] == 0 && k==position){
						//	System.out.println("Match found in Node: "+ (int) (regen_from_nodes[i]) + " in vector no: "+ j + " at position: " + k + " for bit 0");
						rememberBit0Value(regen_from_nodes[i], j);
						//	printBasisVector(regen_from_nodes[i], j);
					}
				}
			}
		}
	}

	public boolean startXORComparison(int part){

		boolean found = false;
		int node1, part1, node2, part2;
		for (int i =0 ; i<mem_bit1.size(); i++){
			for (int j =0; j<mem_bit0.size(); j++){
				System.out.println();
				node1 = mem_bit1.get(i)/10;
				part1 = mem_bit1.get(i)%10;
				node2 = mem_bit0.get(j)/10;
				part2 = mem_bit0.get(j)%10;
				System.out.print("XORing: "); 
				printBasisVector(node1, part1);
				System.out.print(" with: ");
				printBasisVector(node2, part2);
				performXOR(node1, part1, node2, part2);
				if(compareEquality(part)){
					Integer[] final_value = new Integer[2];
					final_value[0] = mem_bit0.get(j);
					final_value[1] = mem_bit1.get(i);
					to_fetch.add(final_value);
					found = true;
					break;
				}
			}

			if (found)
				break;
		}

		return found;
	}

	public boolean compareEquality(int part){
		boolean match = true;
		for (int i =0; i<BasisVector.VECTORSIZE; i++){
			if (BasisVector.list[failed_node][part][i] != gen_vector.get(gen_vector.size()-1)[i]){
				match = false;
				break;
			}

		}
		if(match){
			System.out.println("!!!!!!SUCCESS!!!!!!!");
			return match;
		}
		else
			return match;

	}

	public void rememberBit1Value(int node,int part){
		int value = node*10 + part;
		mem_bit1.add(value);
	}

	public void rememberBit0Value(int node,int part){
		int value = node*10 + part;
		mem_bit0.add(value);
	}

	public void performXOR (int node1, int part1, int node2, int part2){
		System.out.print(" ---> ");
		Integer[] vector = new Integer[BasisVector.VECTORSIZE];
		Integer[] index_track = new Integer[2];
		for(int i =0; i< BasisVector.VECTORSIZE; i++){
			vector[i] = (BasisVector.list[node1][part1][i])^(BasisVector.list[node2][part2][i]);			
			System.out.print(vector[i]);
		}
		gen_vector.add(vector);
		index_track[0] = node1*10+part1;
		index_track[1] = node2*10+part2;
		index_gen_vector.add(index_track);
	}

	public ArrayList<Integer[]> toFetchFinal()
	{
		return to_fetch;
	}

	public void printToFetchFinal(){
		System.out.println("FINAL VALUES TO FETCH");
		for (int i=0; i<to_fetch.size(); i++){
			for(Integer j:to_fetch.get(i)){
				printBasisVector(j/10, j%10);
				//System.out.print("----> " + j);
				System.out.println();
			}
			System.out.println("-------");
		}
	}
}
