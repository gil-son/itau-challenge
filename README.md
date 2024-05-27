# itau-challenge

## Desafio

## Sumário

## Projeto

  <details>
    <summary><h3>Escolhas de desenvolvimento</h3></summary>
      <p>Dentro do fluxo a seguir, existe o tratamento e resiliência onde envolve a utilização de SNS (FIFO) e SQS (FIFO) para capturar as falhas de transferência ocorridas durante as etapas de acesso na API de Cadastro ou API de Contas. 
      Também existe a utilização de um segundo SNS (FIFO) e SQS (FIFO) que armazenam transferências validadas e com o processo de desconto de saldo calculado, mas que ainda não foram registradas na API do Bacen, por alguma indisponibilidade da mesma.</p>
<p>Caso não crie os recursos citados, apenas não poderá ver os dados armazenados nos recursos na AWS, mas a experiência utilizando a API de Transferências será a mesma:</p>
<ol>
    <li> A API de Transferência (Spring Boot) verifica a existência de um cliente acessando a API de Cadastro. Caso o cliente exista, passa para a etapa seguinte (2).
        <ul>
            <li>1.1. Caso a API de Cadastro não possa ser acessada por estar fora, os dados de transferência serão encaminhados via SNS (FIFO) para um tópico, onde uma fila SQS (FIFO) armazenará essa transferência para ser reprocessada quando a API de Cadastro estiver disponível. A API de Transferência informará que não foi possível prosseguir com a transferência, mas que retomará em breve.</li>
            <li>1.2. Caso o cliente não exista, a API de Transferência interrompe o processo e informa que o cliente não existe.</li>
        </ul>
    </li>
    <li> A API de Contas é acessada passando os dados de origem da conta. Se bem-sucedida, passa para a etapa seguinte (3).
        <ul>
            <li>2.1. Caso a API de Contas não possa ser acessada por estar fora, os dados de transferência serão encaminhados via SNS (FIFO) para um tópico, onde uma fila SQS (FIFO) armazenará essa transferência para ser reprocessada quando a API de Contas estiver disponível. A API de Transferência informará que não foi possível prosseguir com a transferência, mas que retomará em breve.</li>
            <li>2.2. Se a conta não for ativa, o processo é interrompido e uma mensagem é retornada informando que a conta não está ativa.</li>
        </ul>
    </li>
    <li> É verificado se a conta possui saldo suficiente para a transferência. Caso sim, é direcionado para a validação seguinte (4).
        <ul>
            <li>3.1. Se a conta não tiver saldo suficiente para a transferência, o processo é interrompido e uma mensagem é retornada informando a ausência de saldo suficiente.</li>
        </ul>
    </li>
    <li> É verificado o limite diário da conta. Caso seja maior que zero e maior que o valor da transferência, seguirá para a etapa de cálculo (5).
        <ul>
            <li>4.1. Caso o valor seja acima do limite diário, a aplicação interromperá o processo informando o cliente.</li>
        </ul>
    </li>
    <li> O cálculo de transferência é feito e o Bacen é notificado.
        <ul>
            <li>5.1. Caso a API do Bacen não possa ser acessada por estar fora, os dados de transferência serão encaminhados via SNS (FIFO) para um tópico exclusivo de falhas do Bacen, onde uma fila SQS (FIFO) armazenará transferências completas, mas que na etapa final tiveram algum problema. Assim, posteriormente, poderão ser utilizadas a partir do fluxo final. E a API de Transferência informará que não foi possível prosseguir com a transferência, mas que retomará em breve.</li>
           <li>5.2. Caso a API do Bacen não possa ser acessada por excesso de requisições, os dados de transferência serão encaminhados via SNS (FIFO) para um tópico exclusivo de falhas do Bacen, onde uma fila SQS (FIFO) armazenará transferências completas, mas que na etapa final tiveram algum problema. Assim, posteriormente, poderão ser utilizadas a partir do fluxo final. E a API de Transferência informará que não foi possível prosseguir com a transferência, mas que retomará em breve, com o diferencial que apresentará o status 429.</li>
        </ul>
    </li>
    <li> A base da API de Transferência faz um registro como prova da sua persistência de dados. E, responde o cliente passando um comprovante (id de transferência) da conclusão do processo de transferência</li>
</ul>

    
  </details>
  <details>
    <summary><h3>Diagrama da resolução</h3></summary>
      <p>Abaixo, é possível ver o diagrama descrito em <b>Escolhas de desenvolvimento:</b></p>
      <div align="center">
        <img src="https://thumbs2.imgbox.com/b9/51/wICRN9lu_t.png">
      </div>
      <p>Caso deseje, é possível obter o arquivo do diagrama acessando <a href="https://github.com/gil-son/itau-challenge/tree/main/utils">aqui</a> (fluxo-aplicacao.drawio).</p>
      <p>(Visualize o diagrama utilizando a ferramenta <a href="https://draw.io">draw.io</a>)</p>
    
  </details>

   <details>
    <summary><h3>Instruções de configurações</h3></summary>

   <details>
    <summary><h4>Passo 1: Preparação do Ambiente - AWS</h4> </summary>   
    <p>Caso você deseje ver o funcionamento do SNS e do SQS, é bacana ter essas configurações. Caso não, o projeto vai funcionar, mas com limitações nos tratamentos de erros. Bom acesse a AWS:</p>
    <ol>
      <li>Dentro do contexto do projeto, é necessário a criação de um usuário para acessar os recursos da AWS. Caso não tenha uma conta na AWS, crie uma conta e configure um usuário administrativo e o modo de acesso 2FA. Veja o vídeo para mais detalhes: <a href="https://www.youtube.com/watch?v=7hcxNAwfhhw">assistir</a> </li>
      <li>Acesse o IAM - > Users -> Create user e escolha um nome</li>
        <div align="center">
          <img src="https://thumbs2.imgbox.com/4d/c0/VryvtGkl_t.png">
        </div>
      <li>É necessário dar algumas permissões ao usuário, então escolha Attach policies directly</li>
        <div align="center">
          <img src="https://thumbs2.imgbox.com/1f/31/Umap4ZMI_t.png">
        </div>
      <li>
        Esolha as permissões de 'AmazonSNSFullAccess' e 'AmazonSQSFullAccess' e confirme
        <div align="center">
          <img src="https://thumbs2.imgbox.com/04/10/1cFNB03z_t.png">
        </div>
      </li>
      <li>
        Em seguida acesse a guia 'Security credential' e cliquei em 'Create access key' para obter as crendenciais de acesso que vão ser utilizadas no projeto
        <div align="center">
          <img src="https://thumbs2.imgbox.com/d0/17/Mafz8yHC_t.png">
        </div>
      </li>
      <li>
        Em seguida selecione a opção para poder utilizar o AWS CLI
        <div align="center">
          <img src="https://thumbs2.imgbox.com/c6/d9/BKJonRho_t.png">
        </div>
      </li>
      <li>
        Defina um nome para as chaves e crie
        <div align="center">
          <img src="https://thumbs2.imgbox.com/e1/38/6MU6imf7_t.png">
        </div>
      </li>
      <li>
        Após a criação, guarde bem as chaves ou faça o download. Não será possível voltar nessa tela
        <div align="center">
          <img src="https://thumbs2.imgbox.com/21/b9/W4Gaxhvx_t.png">
        </div>
      </li>
      <li>
        Agora, vamos criar o SQS que será a fila para armazenar os erros, pequise por SQS e clique no botão para criar
        <div align="center">
          <img src="https://thumbs2.imgbox.com/47/be/371vXrxu_t.png">
        </div>
      </li>
      <li>
        Escolha a opção FIFO (First In First Out) para que o primeiro dado a entrar seja o primeiro a sair da fila. O nome precisa terminar com .fifo
        <div align="center">
          <img src="https://thumbs2.imgbox.com/82/36/NQPXYQXH_t.png">
        </div>
      </li>
      <li>
        Agora vamos criar o tópico do SNS que vai encaminhar as mensagens para a fila. Pesquise por SNS e clique em Topics
        <div align="center">
          <img src="https://thumbs2.imgbox.com/d6/0d/slaNwD7X_t.png">
        </div>
      </li>
      <li>
        Escolha a opção FIFO, para que o primeiro dado a entrar, seja o primeiro dado a sair. O nome precisa terminar com .fifo
        <div align="center">
          <img src="https://thumbs2.imgbox.com/f2/0b/YBZNPDpB_t.png">
        </div>
      </li>
      <li>
        Agora, é necssário criar a assinatura desse tópico, então clique na opção de criar
        <div align="center">
          <img src="https://thumbs2.imgbox.com/fe/0e/l6zN2Dg8_t.png">
        </div>
      </li>
      <li>
        Escolha a opção 'Amazon SQS e em Endpoint seleciono o arn do SQS
        <div align="center">
          <img src="https://thumbs2.imgbox.com/bd/db/lTiLrO3C_t.png">
        </div>
      </li>
      <li>
        Acesso de novo o SQS para vincular o SQS com o SNS via police. Clique na guia 'Access policy' e clique na opção de editar
        <div align="center">
          <img src="https://thumbs2.imgbox.com/94/4c/n6OhCDoZ_t.png">
        </div>
      </li>
      <li>
        Edite a police conforme o script abaixo, lembresse de mudar os parâmetros conforme a sua conta:
        
      ```
      {
        "Version": "2012-10-17",
        "Id": "__default_policy_ID",
        "Statement": [
          {
            "Sid": "__owner_statement",
            "Effect": "Allow",
            "Principal": {
              "AWS": "arn:aws:iam::{id-conta-aws}:root"
            },
            "Action": "SQS:*",
            "Resource": "arn:aws:sqs:us-east-1:{id-conta-aws}:{nome-sqs}.fifo"
          },
          {
            "Sid": "topic-subscription-arn:aws:sns:us-east-1:{id-conta-aws}:{nome-sns}.fifo",
            "Effect": "Allow",
            "Principal": {
              "AWS": "*"
            },
            "Action": "SQS:SendMessage",
            "Resource": "arn:aws:sqs:us-east-1:{id-conta-aws}:{nome-sqs}.fifo",
            "Condition": {
              "ArnLike": {
                "aws:SourceArn": "arn:aws:sns:us-east-1:{id-conta-aws}:{nome-sns}.fifo"
              }
            }
          }
        ]
      }
      ```
      
  </li>
  <li>Repita o processo para criar outro SNS (FIFO) e outro SQS (FIFO), que será exclusivo para as falhas do Basen. </li>
</ol>
  </details>
  
  <details>
    <summary><h4>Passo 2: Preparação do Ambiente - IDE</h4></summary>
      <p>Clone o repositório do projeto para sua máquina local usando o Git:</p> 
     
```
git clone https://github.com/gil-son/itau-challenge.git
cd  itau-challenge
```

  <p>Acesse a sua IDE de preferência</p>
  <ol>
    <li>Selecione o Java na versão 17</li>
    <li>Maven na versão 3.9 ou mais</li>
    <li>Aguarde a IDE atualizar</li>
    <li>Excecute o comando:</li>
    
```
mvn clean install
```
  <li>acesse na sua IDE o local para configurar as variáveis de ambiente e conigure as seguintes variáveis que se encontro no application.properties

```
ARN_FROM_SNS_TRANSFERENCIA= valor;
AWS_ACCESS_KEY_ID= valor;
AWS_ACCESS_SECRET_ID= valor;
AWS_REGION= valor;
ARN_FROM_SNS_TRANSFERENCIA= valor;
ARN_FROM_SNS_BASEN= valor
```
  
  </li>
  <li>Agora execute o projeto para um teste rápido, mas ainda tem a etapa do docker-compose</li>
  </ol>
  </details>
  
  <details>
    <summary><h4>Passo 3: Preparação do Ambiente - Docker Compose</h4></summary>
    <ol>
      <li>Certifique de ter o docker em sua máquina. Caso necessário instale: <a href="https://www.youtube.com/watch?v=YimiSXPzBSs">assistir</a></li>
      <li>Acesse o diretório /wiremock e execute o comando
        
```
     cd wiremock
     docker-compose up
```
  </li>
</ol>
  </details>
 
</details>

 <details>
  <summary><h3>Instruções de execução dos cenários</h3></summary>
    <p>Após as <b>Instruções de configurações</b> o projeto estará apto a executar. Caso não fez as etapas <b>Preparação do Ambiente - AWS</b> o projeto vai excutar, mas em cenários de falhas de conexão não vão funcionar de forma adequada com a mensagem de erro tratada. Caso configurou poderá visualizar o response body e consultar na AWS</p>
    <p>Acesse o postman ou isnomnia e configure a seguinte requisição</p>

  POST - Criação de uma transferência de sucesso

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

            {
              "idCliente": "2ceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
              "valor": 10.00,
              "conta": {
                  "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
                  "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
                    }
            }

    + Response 201:
      
            {
              "id_transferencia": "7c1b4c44-bd13-4789-84cc-63b5ce330f9e"
            }
  
    (application/json)

    <hr/>

    POST - Cliente destinatário não encontrado

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

            {
              "idCliente": "xceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
              "valor": 10.00,
              "conta": {
                  "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
                  "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
                    }
            }

    + Response 500:
      
          {
            "timestamp": "2024-05-27T19:33:30.699699171Z",
            "status": 500,
            "error": "Dado inválido!",
            "path": "/transferencia",
            "errors": [
              {
                  "fieldName": "Regras de negócio",
                  "message": "Cliente com ID {xceb26e9-7b5c-417e-bf75-ffaa66e3a76f} não encontrado"
              }
            ]
          }
  
    (application/json)

<hr/>

    POST - Erro ao buscar dados da conta origem

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

            {
            "idCliente": "bcdd1048-a501-4608-bc82-66d7b4db3600",
            "valor": 1000.00,
            "conta": {
                "idOrigem": "x0d32142-74b7-4aca-9c68-838aeacef96b",
                "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
              }
           }

    + Response 500:
      
          {
            "timestamp": "2024-05-27T19:33:30.699699171Z",
            "status": 500,
            "error": "Dado inválido!",
            "path": "/transferencia",
            "errors": [
              {
                  "fieldName": "Regras de negócio",
                  "message": "Cliente com ID {xceb26e9-7b5c-417e-bf75-ffaa66e3a76f} não encontrado"
              }
            ]
          }
  
    (application/json)

<hr/>

  POST - Limite diário excedido

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

            {
          "idCliente": "2ceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
          "valor": 1000.00,
          "conta": {
              "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
              "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
            }
          }

    + Response 500:
      
            {
            "timestamp": "2024-05-27T19:37:48.935589971Z",
            "status": 500,
            "error": "Dado inválido!",
            "path": "/transferencia",
            "errors": [
                {
                    "fieldName": "Regras de negócio",
                    "message": "Limite diário excedido"
                }
            ]
          }
  
    (application/json)

<hr/>

  POST - Saldo insuficiente

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

           {
            "idCliente": "2ceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
            "valor": 6000.00,
            "conta": {
                "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
                "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
            }
          }

    + Response 500:
      
            {
              "timestamp": "2024-05-27T19:40:43.550765760Z",
              "status": 500,
              "error": "Dado inválido!",
              "path": "/transferencia",
              "errors": [
                  {
                      "fieldName": "Regras de negócio",
                      "message": "Saldo insuficiente"
                  }
              ]
            }
  
    (application/json)
    

<hr/>

  <p>Para provocar as falhas de conexão com as API de Cadastro ou Contas, você pode optar por mudar o path de cada API em wiremock/mappings e para cada arquivo, mude o endpoint da API de Cadastro ou a API de Contas:</p>

  POST - Falhar ao conectar!

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

               {
              "idCliente": "xceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
              "valor": 1000.00,
              "conta": {
                  "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
                  "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
            }
          }

    + Response 500:
      
            {
              "timestamp": "2024-05-27T19:49:45.560288358Z",
              "status": 500,
              "error": "Falhar ao conectar!",
              "path": "/transferencia",
              "errors": [
                  {
                      "fieldName": "Ocorreu uma falha ao conectar com a API externa",
                      "message": "Conexão recusada - A Transação será armazenada e tentaremos automaticamente em breve. Você será notificado."
                  }
              ]
           }
  
    (application/json)

<hr/>

  <p>Para provocar as falhas de conexão com a API do Basem, você pode optar por mudar o path de cada API em wiremock/mappings alterar o endpoint que chama o base:</p>

  POST - Falhar ao conectar - Basen!

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

              {
                "idCliente": "2ceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
                "valor": 300.00,
                "conta": {
                    "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
                    "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
                }
            }

    + Response 500:
      
            {
              "timestamp": "2024-05-27T19:56:30.871882738Z",
              "status": 500,
              "error": "Falhar ao conectar!",
              "path": "/transferencia",
              "errors": [
                  {
                      "fieldName": "Ocorreu uma falha ao conectar com a API externa",
                      "message": "Conexão recusada - A Transação foi processada. Em breve quando o BASEN estiver disponível, receberá o registro."
                  }
              ]
          }
  
    (application/json)

<hr/>

  <p>Para provocar a falha 429, você pode usar alguma ferramenta de alta simulação de requisições. Da mesma forma será enviado um SNS para as falhas do Basen. E armazenado no SQS:</p>

  POST - Falhar ao conectar - Basen!

  http://localhost:8080/transferencia

  + Request (application/json)

    + Body

              {
                "idCliente": "2ceb26e9-7b5c-417e-bf75-ffaa66e3a76f",
                "valor": 300.00,
                "conta": {
                    "idOrigem": "d0d32142-74b7-4aca-9c68-838aeacef96b",
                    "idDestino": "41313d7b-bd75-4c75-9dea-1f4be434007f"
                }
            }

    + Response 429:
      
            {
              "timestamp": "2024-05-27T19:56:30.871882738Z",
              "status": 429,
              "error": "Falhar ao conectar!",
              "path": "/transferencia",
              "errors": [
                  {
                      "fieldName": "Ocorreu uma falha ao conectar com a API externa",
                      "message": "Conexão recusada - A Transação foi processada. Em breve quando o BASEN estiver disponível, receberá o registro."
                  }
              ]
          }
  
    (application/json)
 </details>

## Desafio de Arquitetura
  
  <details>
    <summary><h3>Escolhas dos recursos e resolução</h3></summary>
    <p>Antes de mostrar a solução, é bacana mostrar as divisões dos grupos:</p>
      <ul>
        <li><b>AWS:</b> onde se encontra a maior parte dos recursos</li>
        <li><b>Região:</b> onde se encontra a região da aplicação (fica a escolha)</li>
        <li><b>VPC:</b> responsável por isolar os recursos da parte externa e criar uma camada interna</li>
        <li><b>Subnet pública fora da VPC:</b> responsável pela conexão de chamadas HTTP(s), de entrada/saída ao que for externo e interligando com o que for interno à AWS</li>
        <li><b>Subnet pública dentro da VPC:</b> responsável por receber as chamadas da subnet pública fora da VPC e direcionar para as subnets privadas o que for necessário. Também faz o caminho inverso</li>
      </ul>
  <p>A solução possui o seguinte fluxo com os seguintes recursos:</p>
    <ol>
      <li>O cliente se autentica no Cognito utilizando JWT.</li>
      <li>O Cognito retorna uma credencial para o cliente.</li>
      <li>O cliente envia a credencial para o API Gateway.</li>
      <li>O API Gateway encaminha a credencial para o Cognito verificar.</li>
      <li>O Cognito verifica a credencial e retorna um token autenticado com os níveis de autorização de acesso.</li>
      <li>O API Gateway direciona a chamada para o ALB.</li>
      <li>O ALB balanceia a carga e encaminha a requisição para o Fargate.</li>
      <li>O Fargate (API de Transferência), após realizar as validações necessárias com as APIs de Cadastro e Contas, grava os dados no RDS para garantir a persistência dos dados e concluir sua função.
          <ul>
              <li>8.1. Se houver um problema de comunicação com as APIs de Cadastro ou Contas, o Fargate encaminha os dados para um tópico no SNS (FIFO), que gerencia essas transações pendentes.
                  <ul>
                      <li>8.1.1. O SNS encaminha os dados para o SQS (FIFO).
                          <ul>
                              <li>8.1.1.1. O Fargate consome os dados da fila usando um listener e tenta novamente realizar o fluxo.</li>
                              <li>8.1.1.2. Se houver um problema com a fila SQS, os dados são encaminhados para um SQS (DLQ), onde podem ser recuperados manualmente.</li>
                          </ul>
                      </li>
                  </ul>
              </li>
              <li>8.2. Se houver um problema de comunicação com o BACEN, os dados da transferência, já validados e atualizados, são enviados para um SNS (FIFO), que armazena as transferências com falhas de comunicação com o BACEN.
                  <ul>
                      <li>8.2.1. O SNS encaminha os dados para outro SQS (FIFO).
                          <ul>
                              <li>8.2.1.1. O Fargate consome os dados dessa fila usando um outro listener e tenta novamente realizar o fluxo a partir da comunicação com o BACEN.</li>
                              <li>8.2.1.2. Se houver um problema com esta fila SQS, os dados são encaminhados para outro SQS (DLQ), onde podem ser recuperados manualmente.</li>
                          </ul>
                      </li>
                  </ul>
              </li>
          </ul>
      </li>
      <li>O RDS armazena a transferência, confirmando que a API de Transferência realizou sua função, e retorna uma confirmação ao Fargate.</li>
      <li>O Fargate retorna a resposta via ALB.</li>
      <li>O ALB direciona a resposta para o API Gateway.</li>
      <li>O API Gateway encaminha a resposta para o cliente.</li>
    </ol>

  <p>Os recursos recebem os seguintes complementos:</p>
    <ul>
      <li>As subnets dentro da VPC pública e privada possuem Autoscaling para atender diferentes picos de utilização e demandas</li>
      <li>Os recursos são monitorados via CloudWatch, o que gera logs da aplicação. E estão integrados ao Splunk e Grafana, que permitem a utilização da observabilidade</li>
    </ul>
  </details>

  <details>
    <summary><h3>Diagrama com recursos e resolução</h3></summary>
      <p>Abaixo, é possível ver o diagrama descrito em <b>Escolhas dos recursos e resolução:</b></p>
      <div align="center">
        <img src="https://thumbs2.imgbox.com/b2/d7/Ma9HCam1_t.png">
      </div>
      <p>Caso deseje, é possível obter o arquivo do diagrama acessando <a href="https://github.com/gil-son/itau-challenge/tree/main/utils">aqui</a>. (fluxo-completo.drawio)</p>
      <p>(Visualize o diagrama utilizando a ferramenta <a href="https://draw.io">draw.io</a>)</p>

  </details>  
