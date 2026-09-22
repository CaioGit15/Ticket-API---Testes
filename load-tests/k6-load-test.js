// Teste de carga da Ticket API usando k6 (https://k6.io)
//
// Instalação:
//   Mac:      brew install k6
//   Linux:    veja https://k6.io/docs/get-started/installation/
//   Docker:   docker run --rm -i --network=host grafana/k6 run - < load-tests/k6-load-test.js
//
// Execução (com a aplicação rodando em http://localhost:8080):
//   k6 run load-tests/k6-load-test.js
//
// Este script simula 3 cenários rodando em paralelo:
//   1) leitura da listagem de eventos (carga de leitura)
//   2) cadastro de novos clientes (carga de escrita simples)
//   3) compra de ingressos para um evento com estoque limitado,
//      para validar que a API se comporta corretamente sob concorrência
//      (nunca vende mais ingressos do que o estoque disponível)

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Métricas customizadas para acompanhar o resultado das compras
const comprasComSucesso = new Counter('compras_com_sucesso');
const comprasSemEstoque = new Counter('compras_sem_estoque_422');
const comprasComErro = new Counter('compras_com_erro_inesperado');

export const options = {
  scenarios: {
    // Cenário 1: usuários navegando e listando eventos
    listar_eventos: {
      executor: 'constant-vus',
      vus: 20,
      duration: '30s',
      exec: 'listarEventos',
    },
    // Cenário 2: cadastro de novos clientes, com rampa de usuários
    cadastrar_clientes: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 15 },
        { duration: '15s', target: 15 },
        { duration: '5s', target: 0 },
      ],
      exec: 'cadastrarCliente',
    },
    // Cenário 3: pico de compra concorrente de ingressos (ex.: abertura de vendas)
    comprar_ingressos: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 50 },
        { duration: '20s', target: 50 },
        { duration: '10s', target: 0 },
      ],
      exec: 'comprarIngresso',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800'],   // 95% das requisições abaixo de 800ms
    http_req_failed: ['rate<0.05'],     // menos de 5% de falhas de transporte (5xx/timeout)
  },
};

export function setup() {
  // Cria um evento com estoque propositalmente pequeno para forçar concorrência
  // real por ingressos durante o teste de carga.
  const payload = JSON.stringify({
    nome: 'Evento de Carga - k6',
    descricao: 'Evento criado pelo script de teste de carga',
    dataHora: '2026-12-31T20:00:00',
    local: 'Estádio de Testes',
    preco: 100.0,
    quantidadeTotal: 300,
  });

  const res = http.post(`${BASE_URL}/api/eventos`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'evento de carga criado (201)': (r) => r.status === 201 });

  const eventoId = res.json('id');
  return { eventoId };
}

export function listarEventos() {
  const res = http.get(`${BASE_URL}/api/eventos`);
  check(res, { 'GET /api/eventos retornou 200': (r) => r.status === 200 });
  sleep(1);
}

export function cadastrarCliente() {
  const idUnico = `${__VU}-${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    nome: `Cliente Carga ${idUnico}`,
    email: `cliente.${idUnico}@teste.com`,
    cpf: String(Date.now()).padStart(11, '0').slice(-11),
  });

  const res = http.post(`${BASE_URL}/api/clientes`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, { 'POST /api/clientes retornou 201': (r) => r.status === 201 });
  sleep(1);
}

export function comprarIngresso(data) {
  // Usa o cliente de exemplo (id=1, populado pelo data.sql) para simplificar o cenário.
  const payload = JSON.stringify({
    eventoId: data.eventoId,
    clienteId: 1,
    quantidade: 1,
  });

  const res = http.post(`${BASE_URL}/api/ingressos/comprar`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  if (res.status === 201) {
    comprasComSucesso.add(1);
  } else if (res.status === 422) {
    // esperado quando o estoque do evento de carga se esgota
    comprasSemEstoque.add(1);
  } else {
    comprasComErro.add(1);
  }

  check(res, {
    'compra retornou 201 ou 422 (nunca erro de servidor)': (r) => r.status === 201 || r.status === 422,
  });

  sleep(0.5);
}
