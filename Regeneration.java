import java.util.*;

public class Regeneration {

	int regen_from_nodes[];
	int failed_node;
	int no_nodes_to_regenerate;

	ArrayList<Integer[]> gen_vector;
	ArrayList<Integer[]> intermediate_gen_vector;
	ArrayList <Integer> mem_bit1;
	ArrayList <Integer> mem_bit0;



	public Regeneration() {
		// TODO Auto-generated constructor stub
		mem_bit1 = new ArrayList<Integer>();
		mem_bit0 = new ArrayList<Integer>();	
		gen_vector = new ArrayList<Integer[]>();
		intermediate_gen_vector = new ArrayList<Integer[]>();

	}

	public void input(){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the node number to fail");
		failed_node = scanner.nextInt();
		System.out.println("How many nodes do you want to regenerate from? ");
		no_nodes_to_regenerate = scanner.nextInt();
		regen_from_nodes= new int [no_nodes_to_regenerate];
		for(int i=0; i<no_nodes_to_regenerate;i++){
			System.out.println(i+1+". Enter Node: ");
			regen_from_nodes[i] = scanner.nextInt();
			while(regen_from_nodes[i] == failed_node){
				System.out.println("Node is already failed! Please enter another node.");
				regen_from_nodes[i] = scanner.nextInt();
			}
		}	
		identifyParts();

		System.out.println("!!DONE!!");
	}

	public void identifyParts(){

		for(int i=0; i<BasisVector.PARTSPERNODE; i++  ){
			for(int j= 0; j< BasisVector.VECTORSIZE; j++ ){	
				if (BasisVector.list[failed_node][i][j] == 1){
					System.out.println("BitValue" +BasisVector.list[failed_node][i][j] + " [Failed node "+ failed_node +" ] Vector: " +i+ " at pos: " + (int)(j));
					printBasisVector(failed_node, i);
					findMatchFromGeneratingNodes(i,j);
					boolean match = startXORComparison(i);

					//Flush memory, XORed value stored in gen_vector
					mem_bit0.clear();
					mem_bit1.clear();

					if(match){
						gen_vector.clear();
					}
					else{
						//insert intermediate logic here.
						identifyIntermediate(i,j);
						gen_vector.clear();
						intermediate_gen_vector.clear();
						
					}

					break;
				}
			}

			System.out.println("-----------------------");
		}

	}


	public void identifyIntermediate (int failednode_part, int failednode_firstbit_position){
		for (int k=failednode_firstbit_position+1; k<BasisVector.VECTORSIZE; k++){
			System.out.println();
			System.out.println("Beginning Intermediate Calculation");
			int position = identifyNextBitPosition(failednode_part,failednode_firstbit_position);
			for (int i=0; i<gen_vector.size(); i++){		
				if(gen_vector.get(i)[position] == 0){
					//find vectors with value 1 at position.
					if(findBasisVectorGivenPosition(1, position, gen_vector.get(i),failednode_part)){
						return;
					}
				}

				else{
					//find vectors with value 0 at position.
					if(findBasisVectorGivenPosition(0, position, gen_vector.get(i),failednode_part)){
						return;
					}
				}
			}
			//Control here means, couldn't find in the first intermediate level.
			//copy intermediate_gen_vector to gen_vector and flush gen_vector.
			//update failednode_firstbit_position
			
			copyArrayList(gen_vector, intermediate_gen_vector);
			failednode_firstbit_position = k-1;
			
			
		}
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

	public boolean findBasisVectorGivenPosition(int value, int position, Integer[] generatedVector, int part){
		for (int i=0; i< (no_nodes_to_regenerate); i++){
			for(int j=0; j<BasisVector.PARTSPERNODE; j++){
				for(int k =0; k< BasisVector.VECTORSIZE ;k++){
					if(BasisVector.list[regen_from_nodes[i]][j][k] == value && k==position){
						System.out.print("XORing: ");
						printBasisVector(regen_from_nodes[i], j);
						System.out.print(" with: ");
						printGeneratedVector(generatedVector);
						if(intermediateXOR(generatedVector, BasisVector.list[regen_from_nodes[i]][j],part)){
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
	
	
	public boolean intermediateXOR (Integer[] generatedVector, int[] vector, int part){
		System.out.print(" ----> ");
		Integer[] temp = new Integer[BasisVector.VECTORSIZE];
		for (int i=0; i<BasisVector.VECTORSIZE; i++){
			temp[i]= generatedVector[i]^vector[i];
			intermediate_gen_vector.add(temp);
			System.out.print(temp[i]);
		}
		if(compareEqualityIntermediate(part, intermediate_gen_vector.get(intermediate_gen_vector.size()-1))){
			return true;
		}
		else
			return false;
	}


	public boolean compareEqualityIntermediate(int part, Integer[] intermediateVector){
		boolean match = true;
		for (int i=0; i< BasisVector.VECTORSIZE; i++){
			if(BasisVector.list[failed_node][part][i] != intermediateVector[i] ){
				match = false;
			}
		}

		if (match){
			System.out.print(" !!!INTERMEDIATE MATCH SUCCESS!!! ");
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
		for(int i =0; i< BasisVector.VECTORSIZE; i++){
			vector[i] = (BasisVector.list[node1][part1][i])^(BasisVector.list[node2][part2][i]);
			gen_vector.add(vector);
			System.out.print(gen_vector.get(gen_vector.size()-1)[i]);
		}
	}

	public static void main(String[] args){

		Regeneration regenerate = new Regeneration();
		regenerate.input();

	}

}
