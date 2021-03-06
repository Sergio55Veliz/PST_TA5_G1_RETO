package com.example.amst1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.amst1.ui.home.ItemList;
import com.example.amst1.ui.home.adaptador.RecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LibrosFiltrados extends AppCompatActivity implements RecyclerAdapter.RecyclerItemClick, SearchView.OnQueryTextListener {
    private RecyclerView rvLista;
    private SearchView svSearch;
    private RecyclerAdapter adapter;
    private List<ItemList> items;
    //private TextView txtLibros;
    private String idCategoria;
    private String nomCategoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libros_filtrados);

        Bundle bundle = getIntent().getExtras();
        idCategoria = bundle.getString("id");
        nomCategoria = bundle.getString("nombre");
        this.setTitle(nomCategoria);

        initViews();
        initValues();
        initListener();
    }

    private void initViews(){
        rvLista = findViewById(R.id.rvLista);
        svSearch = findViewById(R.id.svBuscador);
        //txtLibros = (TextView)findViewById(R.id.txt);
    }

    private void initValues() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvLista.setLayoutManager(manager);

        items = getItems();
        adapter = new RecyclerAdapter(items, this);
        rvLista.setAdapter(adapter);
    }

    private String serverConsulta = "https://flightsregister.000webhostapp.com/queriTA5.php";
    private List<ItemList> getItems() {
        List<ItemList> itemLists = new ArrayList<>();

        //Obtener Info de la base de datos
        String[] resultLibros = null;
        String queryCategoLibro = "SELECT * FROM Libro_Categoria WHERE idCatego="+idCategoria;
        try {
            String queryLibro = "SELECT * FROM Libro WHERE ";
            //Obtenemos id de Libros
            String[] datos0 = new String[]{//ahora van 3 variables
                    "query",//parametro diferenciador con la anterior funcion
                    serverConsulta,
                    queryCategoLibro//
            };
            AsyncQuery async0 = new AsyncQuery();
            String[] resultIdLibros = async0.execute(datos0).get()[0].split("\\n");
            String textPrueba = "Categoria_Libro\n";
            for(int i=1; i<resultIdLibros.length; i++){//desde 1 para evitar el encabezado
                String[] infoIds = resultIdLibros[i].split("--");
                //Libro_Categoria: id,idCatego,idLibro
                if(i>1){
                    queryLibro += " OR";
                }
                queryLibro += " id="+infoIds[2];
                textPrueba += resultIdLibros[i]+"\n";
                //ord_por_idImg[Integer.parseInt(infoLibro[4])] = resultLibros[i];
            }



            //
            String[] datos = new String[]{//ahora van 3 variables
                    "query",//parametro diferenciador con la anterior funcion
                    serverConsulta,
                    queryLibro//SELECT * FROM Libro WHERE id=1 id=4 id=5 .... <--Ejemplo
            };
            AsyncQuery async1 = new AsyncQuery();
            //text.setText(async1.execute(datos).get()[0].split("\\n")[3].split("--")[3]);
            resultLibros = async1.execute(datos).get()[0].split("\\n");
            String queryImg = "SELECT * FROM Imagen WHERE";
            // Se crea un HashMap imgMap = {id: datos_del_libro, ... }
            HashMap<String, String> imgMap = new HashMap<>();
            textPrueba += "\nLibros\n";
            for(int i=1; i<resultLibros.length; i++){//desde 1 para evitar el encabezado
                String[] infoLibro = resultLibros[i].split("--");
                //Libro: id,titulo,autor,editorial,idImagen,cantidad,resumen
                if(i>1){
                    queryImg += " OR";
                }
                queryImg += " id="+infoLibro[4];
                textPrueba += resultLibros[i]+"\n";
                // Add keys and values (id, datos_del_libro)
                imgMap.put(infoLibro[4], resultLibros[i]);
            }

            String[] datoImg = new String[]{//ahora van 3 variables
                    "query",//parametro diferenciador con la anterior funcion
                    serverConsulta,
                    queryImg//
            };

            AsyncQuery async2 = new AsyncQuery();
            String[] imagenes = async2.execute(datoImg).get()[0]
                    .split("\\n");
            textPrueba += "\nImagenes\n";
            for(int i=1; i<imagenes.length; i++) {//desde 1 para evitar el encabezado
                String[] imgLine = imagenes[i].split("--");
                String idImg = imgLine[0];
                String[] infoLibro = imgMap.get(idImg).split("--");
                String nomImagen = imgLine[1];
                itemLists.add(new ItemList(infoLibro[1], infoLibro[2], infoLibro[3], nomImagen, infoLibro[6]));
                textPrueba += nomImagen+"\n";
            }


            //txtLibros.setText(textPrueba);

            //Crear los Objetos ItemList que contendrán la info de los libros
            //itemLists.add(new ItemList(resultLibros[1], resultLibros[2], resultLibros[3], imgLibro, resultLibros[6]));
            //itemLists.add(new ItemList(resultLibros[1], resultLibros[2], resultLibros[3], R.drawable.carrie, resultLibros[6]));

        } catch (ExecutionException e) {
            e.printStackTrace();
            //txtLibros.setText(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            //txtLibros.setText(e.getMessage());
        }

        //itemLists.add(new ItemList("Saga de Broly", "Ultima pelicula de DB, peleas epicas.", R.drawable.saga_broly));

        return itemLists;
    }

    /**
     * Ejecuta acciones de cuando se da click al item
     * el item tiene todos los valores del Libro
     * @param item
     */
    @Override
    public void itemClick(ItemList item) {
        /**Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);**/
    }

    /////////////////////////////////////////////////////
    //Métodos para la busqueda de items con el SearchView

    private void initListener() {
        svSearch.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }


}