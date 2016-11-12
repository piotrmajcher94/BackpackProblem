package backpack.impl;

import backpack.Backpack;

import java.util.ArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by adam on 10.11.16.
 * BackPack
 */
public class BackpackImpl implements Backpack <Integer,Integer>{
    private ArrayList<Pair<Integer,Integer>> items;
    private ArrayList<Pair<Integer,Integer>> backpack;
    private Integer backpack_size;

    public BackpackImpl(){
        this.items = new ArrayList<>();
        this.backpack_size = 0;
        this.backpack = new ArrayList<>();
    }
     
    public BackpackImpl(ArrayList<Pair<Integer, Integer>> it, Integer s){
        this.items = it;
        this.backpack_size = s;
        this.backpack = new ArrayList<>();
    }
    @Override
    public Integer getSize() {
        return backpack_size;
    }

    @Override
    public void setSize(Integer s) {
        backpack_size = s;
    }

    @Override
    public ArrayList<Pair<Integer, Integer>> getItems() {
        return items;
    }

    @Override
    public void fillFromFile(String filename) {
        /*
        Na stronie jest napisane, że w pliku wejściowym jest tylko liczba elementów i kolejne pary i nic
        nie jest napisane o rozmiarze plecaka, co wydaje się trochę bez sensu. Dlatego tutaj dodałem, że w pierwszej
        linii oprócz liczby elementów jest też rozmiar plecaka.
        */
        File file = new File(filename);
        int num_items;
        
        try {
            Scanner input = new Scanner(file);
            num_items = input.nextInt(); //wczytujemy liczbę przedmiotów w pliku wejściowym
            this.backpack_size = input.nextInt(); //wczytujemy rozmiar plecaka 
            
            for(int i = 0; i < num_items; i++){
                int size, value;
                
                size = input.nextInt();
                value = input.nextInt();
                
                items.add(new Pair(size, value));
            }
               
        } catch (IOException ex) {
            System.out.printf("ERROR: %s\n", ex);
        }
    }
    
    public int getBackpackValue(){
        
        if( this.backpack.isEmpty() )
            return 0;
        
        int val = 0;
        
        for(int i = 0; i < this.backpack.size(); i++)
            val += this.backpack.get(i).getValue();
        
        return val;
    }
    
    public void packBrute(){
        
        if( this.items.isEmpty() ){
            System.out.println("Nie ma przedmiotów do zapakowania!");
            return;
        }
         
        if( this.items.size() > 27){
            System.out.println("Zbyt wiele przedmiotów do zapakowania. Algorytm przeglądu zupełnego niemożliwy. ");
            return;
        }
        
        int num_items = this.items.size();
        long combinations = 1 << num_items;
        
        long currentBest = 0;
	int current_best_value = 0;
	int current_best_weight = 0;
	
        boolean anything_fits = false;
        
        for (long i  = 0; i < combinations; i++) {
	/*
	Iterujemy po wszystkich mo¿liwoœciach. Ka¿dy bit zmiennej
	permutations odpowiada jednemu elementowi zbioru przedmiotów.
	Jedynka oznacza, ¿e do³¹czamy dany przedmiot, zero, ¿e nie.
	*/
            
            int sum_weight = 0;
            int sum_value = 0;
		
            boolean fits = false;
            long current_perm;
            
            /*Przechodzimy po wszystkich przedmiotach, ¿eby sprawdziæ, czy nale¿¹ do danej kombinacji*/
		for (int k = 0; k < num_items; k++)
		{
			
			current_perm = i;
			/*
			Sprawdzamy przynale¿noœæ
			-> musimy dostaæ siê do k-tego bitu permutations, 
			wiêc trzeba wykonaæ k razy przesuniêcie bitowe 
			w prawo, aby otrzymaæ ten bit na najni¿szej pozycji
	
			Za pomoc¹ AND sprawdzamy, czy ten bit to 0 czy 1, porównuj¹c go z 1
			(jeœli bêdzie jeden to w wyniku bitowego iloczynu logicznego dostaniemy
			1, w innym wypadku zero)
			1000010101000000001
			0000000000000000001
			-------------------
			0000000000000000001
		
			Jeœli równe 0 to znaczy, ¿e przedmiot nie nale¿y do kombinacji i kontynuujemy pêtle*/
			if (((current_perm >> k) & 1) != 1)
				continue;

				

			sum_weight += this.items.get(k).getSize();
			sum_value += this.items.get(k).getValue();
			
			if (sum_weight > this.backpack_size ) 
			{
				fits = false;
				break;
			}

			fits = true;
                        
                        /*Jeœli suma wag przedmiotów kombinacji wiêksza ni¿ pojemnoœæ plecaka to sprawdzamy kolejn¹*/
                        if (!fits)
                            continue;
		
                        anything_fits = true;

                        if (sum_value > current_best_value)
                        {
                            current_best_value = sum_value;
                            current_best_weight = sum_weight;
                            currentBest = i;
                        }
                }
        }
        
        if (!anything_fits){
            System.out.println("Żaden przedmiot nie mieści się w plecaku!");
            return; 
        }
        
        /*Przechodzimy po wszystkich przedmiotach, ¿eby sprawdziæ, czy nale¿¹ do danej kombinacji*/
	for (int k = 0; k < num_items; k++)
	{
		long current_perm = currentBest;
		
		if (((current_perm >> k) & 1) != 1)
			continue;

		/*Dodajemy przedmiot do rozwi¹zania*/

		this.backpack.add(this.items.get(k));
	}
	
        System.out.println("Plecak zapakowany!");
    }
    
    @Override
    public String toString(){
        String ret = "Lista przedmiotów do zapakowania:\n";
        
        if( this.items.isEmpty() )
            ret += "Lista przedmiotów jest pusta!\n";
        else       
            for(int i = 0; i < this.items.size(); i++)
                ret += this.items.get(i).toString() + "\n";
        
        ret += "\n";
        
        ret += "Plecak (rozmiar: " + this.backpack_size + ", wartość przedmiotów: " + this.getBackpackValue() + "):\n";
        
        if( this.backpack.isEmpty() )
            ret += "Plecak jest pusty!\n";
        else
            for(int i = 0; i < this.backpack.size(); i++)
                ret += this.backpack.get(i).toString() + "\n";
        return ret;
    }
}