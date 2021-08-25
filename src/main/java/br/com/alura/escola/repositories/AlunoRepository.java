package br.com.alura.escola.repositories;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

import br.com.alura.escola.codecs.AlunoCodec;
import br.com.alura.escola.models.Aluno;

@Repository
public class AlunoRepository 
{
	
	private MongoClient 	cliente;
	private MongoDatabase 	bancoDados;
	
	private void criarConexao() 
	{
		
		Codec<Document> codec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
		
        AlunoCodec alunoCodec = new AlunoCodec(codec);

        CodecRegistry registro =
        CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(alunoCodec),
                                               MongoClientSettings.getDefaultCodecRegistry());

        MongoClientSettings opcoes = MongoClientSettings.builder()
                .codecRegistry(registro).build();

        cliente 	 = MongoClients.create(opcoes);
        bancoDados = cliente.getDatabase("teste");
        
	}
	
	public void salvar(Aluno aluno)
	{
		
		criarConexao();
		
		try
		{

			MongoCollection<Aluno> alunos = this.bancoDados.getCollection("alunos", Aluno.class);
			
			if (aluno.getId() == null) 
			{
				// novo aluno
				alunos.insertOne(aluno);
			}
			else
			{
				// atualiza aluno
				alunos.updateOne(Filters.eq("_id", aluno.getId()), new Document("$set", aluno));
			}			
		}
		finally
		{
			fecharConexao();	
		}		
		
	}

	
	
	public List<Aluno> obterTodosAlunos()
	{
		
		criarConexao();
		
		try
		{

			MongoCollection<Aluno> alunos = this.bancoDados.getCollection("alunos", Aluno.class);			
			MongoCursor<Aluno> resultados = alunos.find().iterator();					
			return popularAlunos(resultados);
			
		}
		finally
		{
			fecharConexao();			
		}
				
	}
	
	public Aluno obterAlunoPor(String id)
	{
		
		criarConexao();
		
		try
		{
			MongoCollection<Aluno> alunos = this.bancoDados.getCollection("alunos", Aluno.class);			
			return alunos.find(Filters.eq("_id", new ObjectId(id))).first();			
		}
		finally
		{
			fecharConexao();
		}
		
	}

	public List<Aluno> pesquisarPor(String nome) 
	{
		
		criarConexao();
		
		try
		{

			MongoCollection<Aluno> alunoCollection = this.bancoDados.getCollection("alunos" , Aluno.class);			
			MongoCursor<Aluno> resultados = alunoCollection.find(Filters.eq("nome", nome), Aluno.class).iterator();					
			return popularAlunos(resultados);
			
		}
		finally
		{
			fecharConexao();		
		}
		
	}

	private void fecharConexao() 
	{
		this.cliente.close();
	}
	
	private List<Aluno> popularAlunos(MongoCursor<Aluno> resultados)
	{
	
		List<Aluno> alunos = new ArrayList<>();
		
		while (resultados.hasNext())
		{
			alunos.add(resultados.next());
		}
		
		return alunos;
		
	}

	public List<Aluno> pesquisarPor(String classificacao, double nota) 
	{

		criarConexao();
		
		try
		{
			
			MongoCollection<Aluno> alunoCollection = this.bancoDados.getCollection("alunos", Aluno.class);
			
			MongoCursor<Aluno> resultados = null;
			
			if (classificacao.equals("reprovados")) 
			{
				resultados = alunoCollection.find(Filters.lt("notas", nota)).iterator();
			}
			else if(classificacao.equals("aprovados"))
			{
				resultados = alunoCollection.find(Filters.gte("notas", nota)).iterator();
			}		

			return popularAlunos(resultados);			
			
		}
		finally
		{			
			fecharConexao();			
		}	
		
	}

	public List<Aluno> pesquisaPorGeolocalizacao(Aluno aluno) 
	{
	
		criarConexao();
		
		try
		{
		
			MongoCollection<Aluno> alunoCollection = this.bancoDados.getCollection("alunos", Aluno.class);
			
			alunoCollection.createIndex(Indexes.geo2dsphere("contato"));
			
			List<Double> coordinates = aluno.getContato().getCoordinates();
			Point pontoReferencia = new Point(new Position(coordinates.get(0), coordinates.get(1)));
			
			MongoCursor<Aluno> resultados = alunoCollection.find(Filters.nearSphere("contato", pontoReferencia, 2000.0, 0.0)).limit(2).skip(1).iterator();		
					
			return popularAlunos(resultados);
			
		}
		finally
		{			
			fecharConexao();			
		}
				
	}

}
