# itau-challenge

## Desafio

## Sumário

## Projeto

  <details>
    <summary>Escolhas de desenvolvimento</summary>
    
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
      <p>Caso deseje, é possível obter o arquivo do diagrama acessando <a href="">aqui</a>.</p>
      <p>(Visualize o diagrama utilizando a ferramenta <a href="https://draw.io">draw.io</a>)</p>

  </details>  
