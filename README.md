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
    <summary>Diagrama da resolução</summary>
    
  </details>
  <details>
    <summary>Tecnologias</summary>
    
  </details>
  <details>
    <summary>Configurações</summary>
    
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
      <p>Caso deseje, é possível obter o arquivo do diagrama acessando <a href="https://github.com/gil-son/itau-challenge/tree/main/utils">aqui</a>.</p>
      <p>(Visualize o diagrama utilizando a ferramenta <a href="https://draw.io">draw.io</a>)</p>

  </details>  
