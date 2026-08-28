import { I18n } from '../i18n.js'

export const ICON_GROUPS = [
    {
        id: 'finance',
        icons: [
            { key: 'ph-piggy-bank',             pt: 'Poupança',              en: 'Savings',              es: 'Ahorros' },
            { key: 'ph-bank',                   pt: 'Banco',                 en: 'Bank',                 es: 'Banco' },
            { key: 'ph-wallet',                 pt: 'Carteira',              en: 'Wallet',               es: 'Billetera' },
            { key: 'ph-credit-card',            pt: 'Cartão de crédito',     en: 'Credit card',          es: 'Tarjeta de crédito' },
            { key: 'ph-cardholder',             pt: 'Porta-cartões',         en: 'Card holder',          es: 'Tarjetero' },
            { key: 'ph-money',                  pt: 'Dinheiro',              en: 'Money',                es: 'Dinero' },
            { key: 'ph-money-wavy',             pt: 'Cédulas',               en: 'Banknotes',            es: 'Billetes' },
            { key: 'ph-coins',                  pt: 'Moedas',                en: 'Coins',                es: 'Monedas' },
            { key: 'ph-coin',                   pt: 'Troco',                 en: 'Change',               es: 'Cambio' },
            { key: 'ph-hand-coins',             pt: 'Salário',               en: 'Salary',               es: 'Salario' },
            { key: 'ph-hand-deposit',           pt: 'Depósito',              en: 'Deposit',              es: 'Depósito' },
            { key: 'ph-hand-withdraw',          pt: 'Saque',                 en: 'Withdrawal',           es: 'Retiro' },
            { key: 'ph-currency-dollar',        pt: 'Dólar',                 en: 'Dollar',               es: 'Dólar' },
            { key: 'ph-currency-circle-dollar', pt: 'Renda',                 en: 'Income',               es: 'Ingreso' },
            { key: 'ph-currency-eur',           pt: 'Euro',                  en: 'Euro',                 es: 'Euro' },
            { key: 'ph-currency-btc',           pt: 'Criptomoeda',           en: 'Cryptocurrency',       es: 'Criptomoneda' },
            { key: 'ph-trend-up',               pt: 'Investimento',          en: 'Investment',           es: 'Inversión' },
            { key: 'ph-trend-down',             pt: 'Prejuízo',              en: 'Loss',                 es: 'Pérdida' },
            { key: 'ph-chart-line-up',          pt: 'Rendimento',            en: 'Returns',              es: 'Rendimiento' },
            { key: 'ph-chart-pie-slice',        pt: 'Orçamento',             en: 'Budget',               es: 'Presupuesto' },
            { key: 'ph-chart-bar',              pt: 'Estatísticas',          en: 'Statistics',           es: 'Estadísticas' },
            { key: 'ph-receipt',                pt: 'Contas',                en: 'Bills',                es: 'Facturas' },
            { key: 'ph-invoice',                pt: 'Fatura',                en: 'Invoice',              es: 'Factura' },
            { key: 'ph-scales',                 pt: 'Balanço',               en: 'Balance',              es: 'Balance' },
            { key: 'ph-vault',                  pt: 'Cofre',                 en: 'Vault',                es: 'Bóveda' },
            { key: 'ph-treasure-chest',         pt: 'Reserva',               en: 'Nest egg',             es: 'Reserva' },
            { key: 'ph-tip-jar',                pt: 'Vaquinha',              en: 'Tip jar',              es: 'Fondo común' },
            { key: 'ph-calculator',             pt: 'Calculadora',           en: 'Calculator',           es: 'Calculadora' },
            { key: 'ph-percent',                pt: 'Juros',                 en: 'Interest',             es: 'Intereses' },
            { key: 'ph-seal-percent',           pt: 'Desconto',              en: 'Discount',             es: 'Descuento' },
            { key: 'ph-cash-register',          pt: 'Caixa',                 en: 'Cash register',        es: 'Caja' },
            { key: 'ph-contactless-payment',    pt: 'Pagamento por aproximação', en: 'Contactless payment', es: 'Pago sin contacto' },
            { key: 'ph-pix-logo',               pt: 'Pix',                   en: 'Pix',                  es: 'Pix' },
            { key: 'ph-paypal-logo',            pt: 'PayPal',                en: 'PayPal',               es: 'PayPal' },
            { key: 'ph-barcode',                pt: 'Boleto',                en: 'Barcode',              es: 'Código de barras' },
            { key: 'ph-qr-code',                pt: 'QR Code',               en: 'QR code',              es: 'Código QR' },
            { key: 'ph-target',                 pt: 'Meta',                  en: 'Goal',                 es: 'Meta' },
            { key: 'ph-handshake',              pt: 'Acordo',                en: 'Deal',                 es: 'Acuerdo' },
        ]
    },
    {
        id: 'food',
        icons: [
            { key: 'ph-fork-knife',   pt: 'Alimentação',        en: 'Food',            es: 'Alimentación' },
            { key: 'ph-bowl-food',    pt: 'Refeição',           en: 'Meal',            es: 'Comida' },
            { key: 'ph-bowl-steam',   pt: 'Sopa',               en: 'Soup',            es: 'Sopa' },
            { key: 'ph-hamburger',    pt: 'Lanche',             en: 'Burger',          es: 'Hamburguesa' },
            { key: 'ph-pizza',        pt: 'Pizza',              en: 'Pizza',           es: 'Pizza' },
            { key: 'ph-onigiri',      pt: 'Comida japonesa',    en: 'Japanese food',   es: 'Comida japonesa' },
            { key: 'ph-shrimp',       pt: 'Frutos do mar',      en: 'Seafood',         es: 'Mariscos' },
            { key: 'ph-fish',         pt: 'Peixe',              en: 'Fish',            es: 'Pescado' },
            { key: 'ph-egg',          pt: 'Ovos',               en: 'Eggs',            es: 'Huevos' },
            { key: 'ph-bread',        pt: 'Padaria',            en: 'Bakery',          es: 'Panadería' },
            { key: 'ph-cheese',       pt: 'Queijo',             en: 'Cheese',          es: 'Queso' },
            { key: 'ph-carrot',       pt: 'Legumes',            en: 'Vegetables',      es: 'Verduras' },
            { key: 'ph-orange',       pt: 'Frutas',             en: 'Fruit',           es: 'Fruta' },
            { key: 'ph-avocado',      pt: 'Abacate',            en: 'Avocado',         es: 'Aguacate' },
            { key: 'ph-cherries',     pt: 'Frutas vermelhas',   en: 'Berries',         es: 'Frutos rojos' },
            { key: 'ph-pepper',       pt: 'Temperos',           en: 'Spices',          es: 'Especias' },
            { key: 'ph-grains',       pt: 'Grãos',              en: 'Grains',          es: 'Granos' },
            { key: 'ph-cookie',       pt: 'Doces',              en: 'Sweets',          es: 'Dulces' },
            { key: 'ph-cake',         pt: 'Bolo',               en: 'Cake',            es: 'Pastel' },
            { key: 'ph-ice-cream',    pt: 'Sorvete',            en: 'Ice cream',       es: 'Helado' },
            { key: 'ph-popcorn',      pt: 'Pipoca',             en: 'Popcorn',         es: 'Palomitas' },
            { key: 'ph-popsicle',     pt: 'Picolé',             en: 'Popsicle',        es: 'Paleta' },
            { key: 'ph-coffee',       pt: 'Café',               en: 'Coffee',          es: 'Café' },
            { key: 'ph-tea-bag',      pt: 'Chá',                en: 'Tea',             es: 'Té' },
            { key: 'ph-beer-stein',   pt: 'Cerveja',            en: 'Beer',            es: 'Cerveza' },
            { key: 'ph-wine',         pt: 'Vinho',              en: 'Wine',            es: 'Vino' },
            { key: 'ph-martini',      pt: 'Drinks',             en: 'Cocktails',       es: 'Cócteles' },
            { key: 'ph-champagne',    pt: 'Comemoração',        en: 'Champagne',       es: 'Champán' },
            { key: 'ph-pint-glass',   pt: 'Bar',                en: 'Pub',             es: 'Bar' },
            { key: 'ph-cooking-pot',  pt: 'Cozinhar',           en: 'Cooking',         es: 'Cocinar' },
            { key: 'ph-chef-hat',     pt: 'Restaurante',        en: 'Restaurant',      es: 'Restaurante' },
            { key: 'ph-oven',         pt: 'Forno',              en: 'Oven',            es: 'Horno' },
            { key: 'ph-jar',          pt: 'Mantimentos',        en: 'Pantry',          es: 'Despensa' },
            { key: 'ph-basket',       pt: 'Feira',              en: 'Groceries',       es: 'Mercado' },
        ]
    },
    {
        id: 'transport',
        icons: [
            { key: 'ph-car',               pt: 'Carro',              en: 'Car',              es: 'Coche' },
            { key: 'ph-car-profile',       pt: 'Automóvel',          en: 'Automobile',       es: 'Automóvil' },
            { key: 'ph-taxi',              pt: 'Táxi',               en: 'Taxi',             es: 'Taxi' },
            { key: 'ph-bus',               pt: 'Ônibus',             en: 'Bus',              es: 'Autobús' },
            { key: 'ph-van',               pt: 'Van',                en: 'Van',              es: 'Furgoneta' },
            { key: 'ph-truck',             pt: 'Caminhão',           en: 'Truck',            es: 'Camión' },
            { key: 'ph-motorcycle',        pt: 'Moto',               en: 'Motorcycle',       es: 'Motocicleta' },
            { key: 'ph-scooter',           pt: 'Patinete',           en: 'Scooter',          es: 'Patinete' },
            { key: 'ph-moped',             pt: 'Ciclomotor',         en: 'Moped',            es: 'Ciclomotor' },
            { key: 'ph-bicycle',           pt: 'Bicicleta',          en: 'Bicycle',          es: 'Bicicleta' },
            { key: 'ph-train',             pt: 'Metrô',              en: 'Train',            es: 'Tren' },
            { key: 'ph-train-regional',    pt: 'Trem',               en: 'Regional train',   es: 'Tren regional' },
            { key: 'ph-tram',              pt: 'Bonde',              en: 'Tram',             es: 'Tranvía' },
            { key: 'ph-subway',            pt: 'Transporte público', en: 'Subway',           es: 'Metro' },
            { key: 'ph-airplane',          pt: 'Avião',              en: 'Airplane',         es: 'Avión' },
            { key: 'ph-airplane-takeoff',  pt: 'Embarque',           en: 'Takeoff',          es: 'Despegue' },
            { key: 'ph-boat',              pt: 'Barco',              en: 'Boat',             es: 'Barco' },
            { key: 'ph-sailboat',          pt: 'Veleiro',            en: 'Sailboat',         es: 'Velero' },
            { key: 'ph-cable-car',         pt: 'Teleférico',         en: 'Cable car',        es: 'Teleférico' },
            { key: 'ph-gas-pump',          pt: 'Combustível',        en: 'Fuel',             es: 'Combustible' },
            { key: 'ph-charging-station',  pt: 'Carro elétrico',     en: 'EV charging',      es: 'Carga eléctrica' },
            { key: 'ph-car-battery',       pt: 'Bateria do carro',   en: 'Car battery',      es: 'Batería del coche' },
            { key: 'ph-engine',            pt: 'Motor',              en: 'Engine',           es: 'Motor' },
            { key: 'ph-tire',              pt: 'Pneus',              en: 'Tires',            es: 'Neumáticos' },
            { key: 'ph-steering-wheel',    pt: 'Direção',            en: 'Driving',          es: 'Conducción' },
            { key: 'ph-garage',            pt: 'Garagem',            en: 'Garage',           es: 'Garaje' },
            { key: 'ph-road-horizon',      pt: 'Estrada',            en: 'Road trip',        es: 'Carretera' },
            { key: 'ph-traffic-sign',      pt: 'Trânsito',           en: 'Traffic',          es: 'Tráfico' },
            { key: 'ph-traffic-cone',      pt: 'Obras na via',       en: 'Roadworks',        es: 'Obras viales' },
            { key: 'ph-seatbelt',          pt: 'Seguro do carro',    en: 'Car insurance',    es: 'Seguro del coche' },
            { key: 'ph-police-car',        pt: 'Multas',             en: 'Traffic fines',    es: 'Multas' },
            { key: 'ph-trolley',           pt: 'Carrinho',           en: 'Trolley',          es: 'Carrito' },
        ]
    },
    {
        id: 'home',
        icons: [
            { key: 'ph-house',              pt: 'Moradia',              en: 'Housing',            es: 'Vivienda' },
            { key: 'ph-house-line',         pt: 'Casa',                 en: 'House',              es: 'Casa' },
            { key: 'ph-building-apartment', pt: 'Apartamento',          en: 'Apartment',          es: 'Apartamento' },
            { key: 'ph-buildings',          pt: 'Condomínio',           en: 'Buildings',          es: 'Edificios' },
            { key: 'ph-key',                pt: 'Aluguel',              en: 'Rent',               es: 'Alquiler' },
            { key: 'ph-door',               pt: 'Porta',                en: 'Door',               es: 'Puerta' },
            { key: 'ph-couch',              pt: 'Móveis',               en: 'Furniture',          es: 'Muebles' },
            { key: 'ph-armchair',           pt: 'Poltrona',             en: 'Armchair',           es: 'Sillón' },
            { key: 'ph-bed',                pt: 'Cama',                 en: 'Bed',                es: 'Cama' },
            { key: 'ph-chair',              pt: 'Cadeira',              en: 'Chair',              es: 'Silla' },
            { key: 'ph-desk',               pt: 'Escritório em casa',   en: 'Home office',        es: 'Oficina en casa' },
            { key: 'ph-dresser',            pt: 'Cômoda',               en: 'Dresser',            es: 'Cómoda' },
            { key: 'ph-lamp',               pt: 'Iluminação',           en: 'Lighting',           es: 'Iluminación' },
            { key: 'ph-lightbulb',          pt: 'Lâmpada',              en: 'Light bulb',         es: 'Bombilla' },
            { key: 'ph-lightning',          pt: 'Energia',              en: 'Electricity',        es: 'Electricidad' },
            { key: 'ph-drop',               pt: 'Água',                 en: 'Water',              es: 'Agua' },
            { key: 'ph-flame',              pt: 'Gás',                  en: 'Gas',                es: 'Gas' },
            { key: 'ph-solar-panel',        pt: 'Energia solar',        en: 'Solar power',        es: 'Energía solar' },
            { key: 'ph-wifi-high',          pt: 'Internet',             en: 'Internet',           es: 'Internet' },
            { key: 'ph-television',         pt: 'TV a cabo',            en: 'Cable TV',           es: 'TV por cable' },
            { key: 'ph-washing-machine',    pt: 'Lavanderia',           en: 'Laundry',            es: 'Lavandería' },
            { key: 'ph-shower',             pt: 'Chuveiro',             en: 'Shower',             es: 'Ducha' },
            { key: 'ph-bathtub',            pt: 'Banheiro',             en: 'Bathroom',           es: 'Baño' },
            { key: 'ph-toilet',             pt: 'Sanitário',            en: 'Toilet',             es: 'Inodoro' },
            { key: 'ph-toilet-paper',       pt: 'Higiene',              en: 'Hygiene',            es: 'Higiene' },
            { key: 'ph-hand-soap',          pt: 'Sabonete',             en: 'Soap',               es: 'Jabón' },
            { key: 'ph-broom',              pt: 'Limpeza',              en: 'Cleaning',           es: 'Limpieza' },
            { key: 'ph-spray-bottle',       pt: 'Produtos de limpeza',  en: 'Cleaning supplies',  es: 'Productos de limpieza' },
            { key: 'ph-trash',              pt: 'Lixo',                 en: 'Trash',              es: 'Basura' },
            { key: 'ph-recycle',            pt: 'Reciclagem',           en: 'Recycling',          es: 'Reciclaje' },
            { key: 'ph-potted-plant',       pt: 'Plantas',              en: 'Plants',             es: 'Plantas' },
            { key: 'ph-fan',                pt: 'Ventilador',           en: 'Fan',                es: 'Ventilador' },
            { key: 'ph-rug',                pt: 'Tapete',               en: 'Rug',                es: 'Alfombra' },
            { key: 'ph-ladder',             pt: 'Manutenção',           en: 'Maintenance',        es: 'Mantenimiento' },
            { key: 'ph-paint-roller',       pt: 'Pintura',              en: 'Painting',           es: 'Pintura' },
        ]
    },
    {
        id: 'health',
        icons: [
            { key: 'ph-heartbeat',          pt: 'Saúde',            en: 'Health',           es: 'Salud' },
            { key: 'ph-heart',              pt: 'Bem-estar',        en: 'Wellbeing',        es: 'Bienestar' },
            { key: 'ph-pill',               pt: 'Medicamentos',     en: 'Medicine',         es: 'Medicamentos' },
            { key: 'ph-prescription',       pt: 'Receita médica',   en: 'Prescription',     es: 'Receta médica' },
            { key: 'ph-first-aid-kit',      pt: 'Emergência',       en: 'Emergency',        es: 'Emergencia' },
            { key: 'ph-bandaids',           pt: 'Curativos',        en: 'Bandages',         es: 'Vendas' },
            { key: 'ph-stethoscope',        pt: 'Consulta médica',  en: 'Doctor visit',     es: 'Consulta médica' },
            { key: 'ph-syringe',            pt: 'Vacina',           en: 'Vaccine',          es: 'Vacuna' },
            { key: 'ph-tooth',              pt: 'Dentista',         en: 'Dentist',          es: 'Dentista' },
            { key: 'ph-eye',                pt: 'Oftalmologia',     en: 'Eye care',         es: 'Oftalmología' },
            { key: 'ph-eyeglasses',         pt: 'Óculos',           en: 'Glasses',          es: 'Gafas' },
            { key: 'ph-ear',                pt: 'Audição',          en: 'Hearing',          es: 'Audición' },
            { key: 'ph-brain',              pt: 'Terapia',          en: 'Therapy',          es: 'Terapia' },
            { key: 'ph-hospital',           pt: 'Hospital',         en: 'Hospital',         es: 'Hospital' },
            { key: 'ph-ambulance',          pt: 'Ambulância',       en: 'Ambulance',        es: 'Ambulancia' },
            { key: 'ph-wheelchair',         pt: 'Acessibilidade',   en: 'Accessibility',    es: 'Accesibilidad' },
            { key: 'ph-virus',              pt: 'Doença',           en: 'Illness',          es: 'Enfermedad' },
            { key: 'ph-face-mask',          pt: 'Máscara',          en: 'Face mask',        es: 'Mascarilla' },
            { key: 'ph-thermometer',        pt: 'Febre',            en: 'Fever',            es: 'Fiebre' },
            { key: 'ph-flask',              pt: 'Exames',           en: 'Lab tests',        es: 'Análisis' },
            { key: 'ph-test-tube',          pt: 'Laboratório',      en: 'Laboratory',       es: 'Laboratorio' },
            { key: 'ph-microscope',         pt: 'Diagnóstico',      en: 'Diagnosis',        es: 'Diagnóstico' },
            { key: 'ph-bone',               pt: 'Ortopedia',        en: 'Orthopedics',      es: 'Ortopedia' },
            { key: 'ph-dna',                pt: 'Genética',         en: 'Genetics',         es: 'Genética' },
            { key: 'ph-moon',               pt: 'Sono',             en: 'Sleep',            es: 'Sueño' },
            { key: 'ph-barbell',            pt: 'Academia',         en: 'Gym',              es: 'Gimnasio' },
            { key: 'ph-person-simple-run',  pt: 'Corrida',          en: 'Running',          es: 'Correr' },
        ]
    },
    {
        id: 'work',
        icons: [
            { key: 'ph-graduation-cap',      pt: 'Educação',           en: 'Education',       es: 'Educación' },
            { key: 'ph-student',             pt: 'Estudante',          en: 'Student',         es: 'Estudiante' },
            { key: 'ph-books',               pt: 'Livros',             en: 'Books',           es: 'Libros' },
            { key: 'ph-book-open',           pt: 'Leitura',            en: 'Reading',         es: 'Lectura' },
            { key: 'ph-notebook',            pt: 'Caderno',            en: 'Notebook',        es: 'Cuaderno' },
            { key: 'ph-backpack',            pt: 'Material escolar',   en: 'School supplies', es: 'Material escolar' },
            { key: 'ph-chalkboard-teacher',  pt: 'Aulas',              en: 'Classes',         es: 'Clases' },
            { key: 'ph-exam',                pt: 'Prova',              en: 'Exam',            es: 'Examen' },
            { key: 'ph-certificate',         pt: 'Certificado',        en: 'Certificate',     es: 'Certificado' },
            { key: 'ph-medal',               pt: 'Premiação',          en: 'Award',           es: 'Premio' },
            { key: 'ph-trophy',              pt: 'Conquista',          en: 'Achievement',     es: 'Logro' },
            { key: 'ph-translate',           pt: 'Idiomas',            en: 'Languages',       es: 'Idiomas' },
            { key: 'ph-briefcase',           pt: 'Trabalho',           en: 'Work',            es: 'Trabajo' },
            { key: 'ph-briefcase-metal',     pt: 'Negócios',           en: 'Business',        es: 'Negocios' },
            { key: 'ph-building-office',     pt: 'Escritório',         en: 'Office',          es: 'Oficina' },
            { key: 'ph-factory',             pt: 'Indústria',          en: 'Industry',        es: 'Industria' },
            { key: 'ph-storefront',          pt: 'Loja',               en: 'Store',           es: 'Tienda' },
            { key: 'ph-warehouse',           pt: 'Estoque',            en: 'Inventory',       es: 'Inventario' },
            { key: 'ph-presentation-chart',  pt: 'Apresentação',       en: 'Presentation',    es: 'Presentación' },
            { key: 'ph-clipboard-text',      pt: 'Relatório',          en: 'Report',          es: 'Informe' },
            { key: 'ph-folder',              pt: 'Documentos',         en: 'Documents',       es: 'Documentos' },
            { key: 'ph-file-text',           pt: 'Documento',          en: 'Document',        es: 'Documento' },
            { key: 'ph-paperclip',           pt: 'Anexo',              en: 'Attachment',      es: 'Adjunto' },
            { key: 'ph-signature',           pt: 'Contrato',           en: 'Contract',        es: 'Contrato' },
            { key: 'ph-stamp',               pt: 'Carimbo',            en: 'Stamp',           es: 'Sello' },
            { key: 'ph-gavel',               pt: 'Advocacia',          en: 'Legal',           es: 'Legal' },
            { key: 'ph-calendar',            pt: 'Agenda',             en: 'Schedule',        es: 'Agenda' },
            { key: 'ph-clock',               pt: 'Horas',              en: 'Hours',           es: 'Horas' },
            { key: 'ph-timer',               pt: 'Prazo',              en: 'Deadline',        es: 'Plazo' },
            { key: 'ph-users-three',         pt: 'Equipe',             en: 'Team',            es: 'Equipo' },
            { key: 'ph-microphone-stage',    pt: 'Palestra',           en: 'Talk',            es: 'Charla' },
        ]
    },
    {
        id: 'leisure',
        icons: [
            { key: 'ph-game-controller', pt: 'Jogos',            en: 'Games',           es: 'Juegos' },
            { key: 'ph-joystick',        pt: 'Arcade',           en: 'Arcade',          es: 'Arcade' },
            { key: 'ph-puzzle-piece',    pt: 'Quebra-cabeça',    en: 'Puzzle',          es: 'Rompecabezas' },
            { key: 'ph-dice-five',       pt: 'Jogos de mesa',    en: 'Board games',     es: 'Juegos de mesa' },
            { key: 'ph-cards',           pt: 'Cartas',           en: 'Cards',           es: 'Cartas' },
            { key: 'ph-poker-chip',      pt: 'Apostas',          en: 'Betting',         es: 'Apuestas' },
            { key: 'ph-film-strip',      pt: 'Cinema',           en: 'Movies',          es: 'Cine' },
            { key: 'ph-film-slate',      pt: 'Filmes',           en: 'Film',            es: 'Películas' },
            { key: 'ph-monitor-play',    pt: 'Streaming',        en: 'Streaming',       es: 'Streaming' },
            { key: 'ph-music-notes',     pt: 'Música',           en: 'Music',           es: 'Música' },
            { key: 'ph-headphones',      pt: 'Fones de ouvido',  en: 'Headphones',      es: 'Auriculares' },
            { key: 'ph-microphone',      pt: 'Karaokê',          en: 'Karaoke',         es: 'Karaoke' },
            { key: 'ph-guitar',          pt: 'Instrumentos',     en: 'Instruments',     es: 'Instrumentos' },
            { key: 'ph-piano-keys',      pt: 'Piano',            en: 'Piano',           es: 'Piano' },
            { key: 'ph-vinyl-record',    pt: 'Discos',           en: 'Records',         es: 'Discos' },
            { key: 'ph-speaker-high',    pt: 'Som',              en: 'Audio',           es: 'Audio' },
            { key: 'ph-radio',           pt: 'Rádio',            en: 'Radio',           es: 'Radio' },
            { key: 'ph-disco-ball',      pt: 'Balada',           en: 'Nightlife',       es: 'Discoteca' },
            { key: 'ph-confetti',        pt: 'Festa',            en: 'Party',           es: 'Fiesta' },
            { key: 'ph-balloon',         pt: 'Aniversário',      en: 'Birthday',        es: 'Cumpleaños' },
            { key: 'ph-gift',            pt: 'Presentes',        en: 'Gifts',           es: 'Regalos' },
            { key: 'ph-mask-happy',      pt: 'Teatro',           en: 'Theater',         es: 'Teatro' },
            { key: 'ph-paint-brush',     pt: 'Arte',             en: 'Art',             es: 'Arte' },
            { key: 'ph-palette',         pt: 'Hobby',            en: 'Hobby',           es: 'Pasatiempo' },
            { key: 'ph-camera',          pt: 'Fotografia',       en: 'Photography',     es: 'Fotografía' },
            { key: 'ph-video-camera',    pt: 'Vídeo',            en: 'Video',           es: 'Vídeo' },
            { key: 'ph-ticket',          pt: 'Ingressos',        en: 'Tickets',         es: 'Entradas' },
            { key: 'ph-tent',            pt: 'Camping',          en: 'Camping',         es: 'Camping' },
            { key: 'ph-campfire',        pt: 'Fogueira',         en: 'Campfire',        es: 'Fogata' },
            { key: 'ph-binoculars',      pt: 'Passeio',          en: 'Sightseeing',     es: 'Paseo' },
        ]
    },
    {
        id: 'sports',
        icons: [
            { key: 'ph-soccer-ball',              pt: 'Futebol',            en: 'Soccer',          es: 'Fútbol' },
            { key: 'ph-basketball',               pt: 'Basquete',           en: 'Basketball',      es: 'Baloncesto' },
            { key: 'ph-volleyball',               pt: 'Vôlei',              en: 'Volleyball',      es: 'Voleibol' },
            { key: 'ph-football',                 pt: 'Futebol americano',  en: 'Football',        es: 'Fútbol americano' },
            { key: 'ph-baseball',                 pt: 'Beisebol',           en: 'Baseball',        es: 'Béisbol' },
            { key: 'ph-tennis-ball',              pt: 'Tênis',              en: 'Tennis',          es: 'Tenis' },
            { key: 'ph-ping-pong',                pt: 'Tênis de mesa',      en: 'Table tennis',    es: 'Tenis de mesa' },
            { key: 'ph-racquet',                  pt: 'Raquete',            en: 'Racquet',         es: 'Raqueta' },
            { key: 'ph-golf',                     pt: 'Golfe',              en: 'Golf',            es: 'Golf' },
            { key: 'ph-bowling-ball',             pt: 'Boliche',            en: 'Bowling',         es: 'Bolos' },
            { key: 'ph-boxing-glove',             pt: 'Boxe',               en: 'Boxing',          es: 'Boxeo' },
            { key: 'ph-hockey',                   pt: 'Hóquei',             en: 'Hockey',          es: 'Hockey' },
            { key: 'ph-cricket',                  pt: 'Críquete',           en: 'Cricket',         es: 'Críquet' },
            { key: 'ph-court-basketball',         pt: 'Quadra',             en: 'Court',           es: 'Cancha' },
            { key: 'ph-swimming-pool',            pt: 'Piscina',            en: 'Swimming pool',   es: 'Piscina' },
            { key: 'ph-person-simple-swim',       pt: 'Natação',            en: 'Swimming',        es: 'Natación' },
            { key: 'ph-person-simple-bike',       pt: 'Ciclismo',           en: 'Cycling',         es: 'Ciclismo' },
            { key: 'ph-person-simple-hike',       pt: 'Trilha',             en: 'Hiking',          es: 'Senderismo' },
            { key: 'ph-person-simple-walk',       pt: 'Caminhada',          en: 'Walking',         es: 'Caminata' },
            { key: 'ph-person-simple-ski',        pt: 'Esqui',              en: 'Skiing',          es: 'Esquí' },
            { key: 'ph-person-simple-snowboard',  pt: 'Snowboard',          en: 'Snowboarding',    es: 'Snowboard' },
            { key: 'ph-person-simple-tai-chi',    pt: 'Yoga',               en: 'Yoga',            es: 'Yoga' },
            { key: 'ph-sneaker-move',             pt: 'Treino',             en: 'Workout',         es: 'Entrenamiento' },
        ]
    },
    {
        id: 'shopping',
        icons: [
            { key: 'ph-shopping-cart',  pt: 'Compras',         en: 'Shopping',      es: 'Compras' },
            { key: 'ph-shopping-bag',   pt: 'Sacola',          en: 'Shopping bag',  es: 'Bolsa de compras' },
            { key: 'ph-tag',            pt: 'Geral',           en: 'General',       es: 'General' },
            { key: 'ph-package',        pt: 'Encomenda',       en: 'Package',       es: 'Paquete' },
            { key: 'ph-moped-front',    pt: 'Delivery',        en: 'Delivery',      es: 'Entrega a domicilio' },
            { key: 'ph-t-shirt',        pt: 'Roupas',          en: 'Clothes',       es: 'Ropa' },
            { key: 'ph-shirt-folded',   pt: 'Vestuário',       en: 'Apparel',       es: 'Vestimenta' },
            { key: 'ph-hoodie',         pt: 'Moletom',         en: 'Hoodie',        es: 'Sudadera' },
            { key: 'ph-dress',          pt: 'Vestido',         en: 'Dress',         es: 'Vestido' },
            { key: 'ph-pants',          pt: 'Calças',          en: 'Pants',         es: 'Pantalones' },
            { key: 'ph-coat-hanger',    pt: 'Guarda-roupa',    en: 'Wardrobe',      es: 'Ropero' },
            { key: 'ph-sneaker',        pt: 'Calçados',        en: 'Shoes',         es: 'Zapatos' },
            { key: 'ph-boot',           pt: 'Botas',           en: 'Boots',         es: 'Botas' },
            { key: 'ph-high-heel',      pt: 'Sapatos sociais', en: 'Heels',         es: 'Tacones' },
            { key: 'ph-sock',           pt: 'Meias',           en: 'Socks',         es: 'Calcetines' },
            { key: 'ph-belt',           pt: 'Cinto',           en: 'Belt',          es: 'Cinturón' },
            { key: 'ph-handbag',        pt: 'Bolsa',           en: 'Handbag',       es: 'Bolso' },
            { key: 'ph-baseball-cap',   pt: 'Boné',            en: 'Cap',           es: 'Gorra' },
            { key: 'ph-beanie',         pt: 'Gorro',           en: 'Beanie',        es: 'Gorro' },
            { key: 'ph-sunglasses',     pt: 'Óculos de sol',   en: 'Sunglasses',    es: 'Gafas de sol' },
            { key: 'ph-watch',          pt: 'Relógio',         en: 'Watch',         es: 'Reloj' },
            { key: 'ph-diamond',        pt: 'Joias',           en: 'Jewelry',       es: 'Joyas' },
            { key: 'ph-crown',          pt: 'Luxo',            en: 'Luxury',        es: 'Lujo' },
            { key: 'ph-scissors',       pt: 'Cabeleireiro',    en: 'Haircut',       es: 'Peluquería' },
            { key: 'ph-hair-dryer',     pt: 'Beleza',          en: 'Beauty',        es: 'Belleza' },
        ]
    },
    {
        id: 'tech',
        icons: [
            { key: 'ph-device-mobile',    pt: 'Celular',                 en: 'Mobile phone',    es: 'Celular' },
            { key: 'ph-device-tablet',    pt: 'Tablet',                  en: 'Tablet',          es: 'Tableta' },
            { key: 'ph-laptop',           pt: 'Notebook',                en: 'Laptop',          es: 'Portátil' },
            { key: 'ph-desktop',          pt: 'Computador',              en: 'Computer',        es: 'Computadora' },
            { key: 'ph-monitor',          pt: 'Tecnologia',              en: 'Technology',      es: 'Tecnología' },
            { key: 'ph-keyboard',         pt: 'Teclado',                 en: 'Keyboard',        es: 'Teclado' },
            { key: 'ph-mouse',            pt: 'Mouse',                   en: 'Mouse',           es: 'Ratón' },
            { key: 'ph-printer',          pt: 'Impressora',              en: 'Printer',         es: 'Impresora' },
            { key: 'ph-hard-drive',       pt: 'Armazenamento',           en: 'Storage',         es: 'Almacenamiento' },
            { key: 'ph-cloud',            pt: 'Nuvem',                   en: 'Cloud',           es: 'Nube' },
            { key: 'ph-database',         pt: 'Dados',                   en: 'Data',            es: 'Datos' },
            { key: 'ph-cpu',              pt: 'Processador',             en: 'Processor',       es: 'Procesador' },
            { key: 'ph-robot',            pt: 'Automação',               en: 'Automation',      es: 'Automatización' },
            { key: 'ph-head-circuit',     pt: 'Inteligência artificial', en: 'AI',              es: 'Inteligencia artificial' },
            { key: 'ph-headset',          pt: 'Headset',                 en: 'Headset',         es: 'Auriculares con micrófono' },
            { key: 'ph-webcam',           pt: 'Webcam',                  en: 'Webcam',          es: 'Cámara web' },
            { key: 'ph-usb',              pt: 'USB',                     en: 'USB',             es: 'USB' },
            { key: 'ph-plug',             pt: 'Tomada',                  en: 'Plug',            es: 'Enchufe' },
            { key: 'ph-battery-full',     pt: 'Bateria',                 en: 'Battery',         es: 'Batería' },
            { key: 'ph-cell-tower',       pt: 'Telefonia',               en: 'Mobile network',  es: 'Telefonía' },
            { key: 'ph-phone',            pt: 'Telefone',                en: 'Phone',           es: 'Teléfono' },
            { key: 'ph-envelope',         pt: 'E-mail',                  en: 'Email',           es: 'Correo' },
            { key: 'ph-chat-circle',      pt: 'Mensagens',               en: 'Messages',        es: 'Mensajes' },
            { key: 'ph-video-conference', pt: 'Videochamada',            en: 'Video call',      es: 'Videollamada' },
            { key: 'ph-paper-plane-tilt', pt: 'Enviar',                  en: 'Send',            es: 'Enviar' },
            { key: 'ph-globe',            pt: 'Web',                     en: 'Web',             es: 'Web' },
            { key: 'ph-link',             pt: 'Link',                    en: 'Link',            es: 'Enlace' },
            { key: 'ph-terminal-window',  pt: 'Programação',             en: 'Coding',          es: 'Programación' },
            { key: 'ph-code',             pt: 'Código',                  en: 'Code',            es: 'Código' },
            { key: 'ph-bug',              pt: 'Suporte',                 en: 'Support',         es: 'Soporte' },
            { key: 'ph-gear',             pt: 'Configurações',           en: 'Settings',        es: 'Configuración' },
            { key: 'ph-shield-check',     pt: 'Segurança',               en: 'Security',        es: 'Seguridad' },
            { key: 'ph-lock',             pt: 'Bloqueio',                en: 'Lock',            es: 'Bloqueo' },
            { key: 'ph-fingerprint',      pt: 'Biometria',               en: 'Biometrics',      es: 'Biometría' },
            { key: 'ph-repeat',           pt: 'Assinatura',              en: 'Subscription',    es: 'Suscripción' },
        ]
    },
    {
        id: 'travel',
        icons: [
            { key: 'ph-airplane-in-flight',    pt: 'Viagem',           en: 'Travel',          es: 'Viaje' },
            { key: 'ph-suitcase',              pt: 'Bagagem',          en: 'Luggage',         es: 'Equipaje' },
            { key: 'ph-suitcase-rolling',      pt: 'Mala',             en: 'Suitcase',        es: 'Maleta' },
            { key: 'ph-identification-card',   pt: 'Documentos',       en: 'ID documents',    es: 'Documentos' },
            { key: 'ph-map-pin',               pt: 'Local',            en: 'Location',        es: 'Ubicación' },
            { key: 'ph-map-trifold',           pt: 'Mapa',             en: 'Map',             es: 'Mapa' },
            { key: 'ph-compass',               pt: 'Bússola',          en: 'Compass',         es: 'Brújula' },
            { key: 'ph-gps-fix',               pt: 'Localização',      en: 'GPS',             es: 'Localización' },
            { key: 'ph-signpost',              pt: 'Direções',         en: 'Directions',      es: 'Direcciones' },
            { key: 'ph-globe-hemisphere-west', pt: 'Mundo',            en: 'World',           es: 'Mundo' },
            { key: 'ph-call-bell',             pt: 'Hotel',            en: 'Hotel',           es: 'Hotel' },
            { key: 'ph-sun',                   pt: 'Férias',           en: 'Vacation',        es: 'Vacaciones' },
            { key: 'ph-beach-ball',            pt: 'Praia',            en: 'Beach',           es: 'Playa' },
            { key: 'ph-island',                pt: 'Ilha',             en: 'Island',          es: 'Isla' },
            { key: 'ph-tree-palm',             pt: 'Tropical',         en: 'Tropical',        es: 'Tropical' },
            { key: 'ph-city',                  pt: 'Cidade',           en: 'City',            es: 'Ciudad' },
            { key: 'ph-bridge',                pt: 'Ponte',            en: 'Bridge',          es: 'Puente' },
            { key: 'ph-castle-turret',         pt: 'Castelo',          en: 'Castle',          es: 'Castillo' },
            { key: 'ph-lighthouse',            pt: 'Farol',            en: 'Lighthouse',      es: 'Faro' },
            { key: 'ph-church',                pt: 'Igreja',           en: 'Church',          es: 'Iglesia' },
            { key: 'ph-mosque',                pt: 'Mesquita',         en: 'Mosque',          es: 'Mezquita' },
            { key: 'ph-synagogue',             pt: 'Sinagoga',         en: 'Synagogue',       es: 'Sinagoga' },
            { key: 'ph-park',                  pt: 'Parque',           en: 'Park',            es: 'Parque' },
            { key: 'ph-picnic-table',          pt: 'Piquenique',       en: 'Picnic',          es: 'Picnic' },
            { key: 'ph-farm',                  pt: 'Fazenda',          en: 'Farm',            es: 'Granja' },
            { key: 'ph-barn',                  pt: 'Celeiro',          en: 'Barn',            es: 'Granero' },
        ]
    },
    {
        id: 'people',
        icons: [
            { key: 'ph-user',            pt: 'Perfil',        en: 'Profile',      es: 'Perfil' },
            { key: 'ph-user-circle',     pt: 'Conta',         en: 'Account',      es: 'Cuenta' },
            { key: 'ph-person',          pt: 'Pessoa',        en: 'Person',       es: 'Persona' },
            { key: 'ph-users',           pt: 'Pessoas',       en: 'People',       es: 'Personas' },
            { key: 'ph-users-four',      pt: 'Família',       en: 'Family',       es: 'Familia' },
            { key: 'ph-baby',            pt: 'Filhos',        en: 'Children',     es: 'Hijos' },
            { key: 'ph-baby-carriage',   pt: 'Bebê',          en: 'Baby',         es: 'Bebé' },
            { key: 'ph-heart-straight',  pt: 'Amor',          en: 'Love',         es: 'Amor' },
            { key: 'ph-hand-heart',      pt: 'Doação',        en: 'Donation',     es: 'Donación' },
            { key: 'ph-hands-praying',   pt: 'Religião',      en: 'Faith',        es: 'Fe' },
            { key: 'ph-smiley',          pt: 'Humor',         en: 'Mood',         es: 'Ánimo' },
            { key: 'ph-paw-print',       pt: 'Pet',           en: 'Pet',          es: 'Mascota' },
            { key: 'ph-dog',             pt: 'Cachorro',      en: 'Dog',          es: 'Perro' },
            { key: 'ph-cat',             pt: 'Gato',          en: 'Cat',          es: 'Gato' },
            { key: 'ph-horse',           pt: 'Cavalo',        en: 'Horse',        es: 'Caballo' },
        ]
    },
    {
        id: 'nature',
        icons: [
            { key: 'ph-tree',            pt: 'Árvore',        en: 'Tree',         es: 'Árbol' },
            { key: 'ph-tree-evergreen',  pt: 'Pinheiro',      en: 'Pine tree',    es: 'Pino' },
            { key: 'ph-plant',           pt: 'Planta',        en: 'Plant',        es: 'Planta' },
            { key: 'ph-leaf',            pt: 'Sustentável',   en: 'Eco',          es: 'Ecológico' },
            { key: 'ph-flower',          pt: 'Flor',          en: 'Flower',       es: 'Flor' },
            { key: 'ph-flower-tulip',    pt: 'Jardim',        en: 'Garden',       es: 'Jardín' },
            { key: 'ph-cactus',          pt: 'Cacto',         en: 'Cactus',       es: 'Cactus' },
            { key: 'ph-mountains',       pt: 'Montanha',      en: 'Mountains',    es: 'Montaña' },
            { key: 'ph-waves',           pt: 'Mar',           en: 'Sea',          es: 'Mar' },
            { key: 'ph-sun-horizon',     pt: 'Amanhecer',     en: 'Sunrise',      es: 'Amanecer' },
            { key: 'ph-cloud-sun',       pt: 'Clima',         en: 'Weather',      es: 'Clima' },
            { key: 'ph-cloud-rain',      pt: 'Chuva',         en: 'Rain',         es: 'Lluvia' },
            { key: 'ph-cloud-lightning', pt: 'Tempestade',    en: 'Storm',        es: 'Tormenta' },
            { key: 'ph-cloud-snow',      pt: 'Neve',          en: 'Snow',         es: 'Nieve' },
            { key: 'ph-snowflake',       pt: 'Frio',          en: 'Cold',         es: 'Frío' },
            { key: 'ph-wind',            pt: 'Vento',         en: 'Wind',         es: 'Viento' },
            { key: 'ph-umbrella',        pt: 'Guarda-chuva',  en: 'Umbrella',     es: 'Paraguas' },
            { key: 'ph-rainbow',         pt: 'Arco-íris',     en: 'Rainbow',      es: 'Arcoíris' },
            { key: 'ph-moon-stars',      pt: 'Noite',         en: 'Night',        es: 'Noche' },
            { key: 'ph-planet',          pt: 'Espaço',        en: 'Space',        es: 'Espacio' },
            { key: 'ph-butterfly',       pt: 'Borboleta',     en: 'Butterfly',    es: 'Mariposa' },
            { key: 'ph-bug-beetle',      pt: 'Inseto',        en: 'Insect',       es: 'Insecto' },
            { key: 'ph-bird',            pt: 'Pássaro',       en: 'Bird',         es: 'Pájaro' },
            { key: 'ph-fish-simple',     pt: 'Aquário',       en: 'Aquarium',     es: 'Acuario' },
            { key: 'ph-cow',             pt: 'Gado',          en: 'Cattle',       es: 'Ganado' },
            { key: 'ph-rabbit',          pt: 'Coelho',        en: 'Rabbit',       es: 'Conejo' },
        ]
    },
    {
        id: 'tools',
        icons: [
            { key: 'ph-toolbox',           pt: 'Ferramentas',           en: 'Tools',            es: 'Herramientas' },
            { key: 'ph-wrench',            pt: 'Reparos',               en: 'Repairs',          es: 'Reparaciones' },
            { key: 'ph-hammer',            pt: 'Reforma',               en: 'Renovation',       es: 'Reforma' },
            { key: 'ph-screwdriver',       pt: 'Montagem',              en: 'Assembly',         es: 'Montaje' },
            { key: 'ph-pipe-wrench',       pt: 'Encanamento',           en: 'Plumbing',         es: 'Fontanería' },
            { key: 'ph-nut',               pt: 'Peças',                 en: 'Parts',            es: 'Piezas' },
            { key: 'ph-paint-bucket',      pt: 'Tinta',                 en: 'Paint',            es: 'Pintura' },
            { key: 'ph-ruler',             pt: 'Medidas',               en: 'Measurements',     es: 'Medidas' },
            { key: 'ph-pencil-ruler',      pt: 'Projeto',               en: 'Design',           es: 'Diseño' },
            { key: 'ph-blueprint',         pt: 'Planta',                en: 'Blueprint',        es: 'Plano' },
            { key: 'ph-crane',             pt: 'Construção',            en: 'Construction',     es: 'Construcción' },
            { key: 'ph-bulldozer',         pt: 'Obra',                  en: 'Earthworks',       es: 'Obra' },
            { key: 'ph-hard-hat',          pt: 'Segurança do trabalho', en: 'Workplace safety', es: 'Seguridad laboral' },
            { key: 'ph-shovel',            pt: 'Jardinagem',            en: 'Gardening',        es: 'Jardinería' },
            { key: 'ph-tractor',           pt: 'Agricultura',           en: 'Agriculture',      es: 'Agricultura' },
            { key: 'ph-fire-extinguisher', pt: 'Prevenção',             en: 'Fire safety',      es: 'Prevención' },
            { key: 'ph-siren',             pt: 'Alerta',                en: 'Alert',            es: 'Alerta' },
            { key: 'ph-security-camera',   pt: 'Monitoramento',         en: 'Surveillance',     es: 'Vigilancia' },
            { key: 'ph-truck-trailer',     pt: 'Mudança',               en: 'Moving',           es: 'Mudanza' },
        ]
    },
    {
        id: 'symbols',
        icons: [
            { key: 'ph-star',               pt: 'Estrela',       en: 'Star',          es: 'Estrella' },
            { key: 'ph-star-four',          pt: 'Destaque',      en: 'Highlight',     es: 'Destacado' },
            { key: 'ph-sparkle',            pt: 'Especial',      en: 'Special',       es: 'Especial' },
            { key: 'ph-flag',               pt: 'Bandeira',      en: 'Flag',          es: 'Bandera' },
            { key: 'ph-bookmark',           pt: 'Favorito',      en: 'Bookmark',      es: 'Marcador' },
            { key: 'ph-check-circle',       pt: 'Concluído',     en: 'Done',          es: 'Completado' },
            { key: 'ph-x-circle',           pt: 'Cancelado',     en: 'Cancelled',     es: 'Cancelado' },
            { key: 'ph-warning',            pt: 'Atenção',       en: 'Warning',       es: 'Atención' },
            { key: 'ph-info',               pt: 'Informação',    en: 'Information',   es: 'Información' },
            { key: 'ph-question',           pt: 'Outros',        en: 'Other',         es: 'Otros' },
            { key: 'ph-prohibit',           pt: 'Proibido',      en: 'Blocked',       es: 'Prohibido' },
            { key: 'ph-fire',               pt: 'Em alta',       en: 'Trending',      es: 'Tendencia' },
            { key: 'ph-lifebuoy',           pt: 'Ajuda',         en: 'Help',          es: 'Ayuda' },
            { key: 'ph-lightbulb-filament', pt: 'Ideia',         en: 'Idea',          es: 'Idea' },
            { key: 'ph-magic-wand',         pt: 'Automático',    en: 'Automatic',     es: 'Automático' },
            { key: 'ph-infinity',           pt: 'Ilimitado',     en: 'Unlimited',     es: 'Ilimitado' },
            { key: 'ph-hourglass',          pt: 'Pendente',      en: 'Pending',       es: 'Pendiente' },
            { key: 'ph-calendar-check',     pt: 'Agendado',      en: 'Scheduled',     es: 'Programado' },
            { key: 'ph-arrows-clockwise',   pt: 'Recorrente',    en: 'Recurring',     es: 'Recurrente' },
            { key: 'ph-seal-check',         pt: 'Verificado',    en: 'Verified',      es: 'Verificado' },
            { key: 'ph-thumbs-up',          pt: 'Aprovado',      en: 'Approved',      es: 'Aprobado' },
            { key: 'ph-skull',              pt: 'Risco',         en: 'Risk',          es: 'Riesgo' },
            { key: 'ph-anchor',             pt: 'Fixo',          en: 'Fixed',         es: 'Fijo' },
            { key: 'ph-yin-yang',           pt: 'Equilíbrio',    en: 'Balance',       es: 'Equilibrio' },
            { key: 'ph-squares-four',       pt: 'Categorias',    en: 'Categories',    es: 'Categorías' },
            { key: 'ph-list-checks',        pt: 'Tarefas',       en: 'Tasks',         es: 'Tareas' },
            { key: 'ph-note-pencil',        pt: 'Anotação',      en: 'Note',          es: 'Nota' },
            { key: 'ph-push-pin',           pt: 'Fixado',        en: 'Pinned',        es: 'Fijado' },
            { key: 'ph-bell',               pt: 'Lembrete',      en: 'Reminder',      es: 'Recordatorio' },
            { key: 'ph-megaphone',          pt: 'Divulgação',    en: 'Announcement',  es: 'Anuncio' },
            { key: 'ph-share-network',      pt: 'Compartilhar',  en: 'Share',         es: 'Compartir' },
        ]
    },
]

export const CATEGORY_ICONS = ICON_GROUPS.flatMap(group => group.icons.map(icon => ({ ...icon, group: group.id })))

const normalize = value => (value ?? '').toString().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()

export function iconLabel(icon) {
    if (!icon) return ''
    return icon[I18n.getLanguage()] || icon.pt || icon.key.replace('ph-', '')
}

export function iconLabelByKey(iconKey) {
    const found = CATEGORY_ICONS.find(icon => icon.key === iconKey)
    return found ? iconLabel(found) : (iconKey ?? '').replace('ph-', '')
}

export function iconGroupLabel(groupId) {
    return I18n.t('iconGroup' + groupId.charAt(0).toUpperCase() + groupId.slice(1))
}

export function filterIconGroups(query) {
    const normalized = normalize(query).trim()
    if (!normalized) return ICON_GROUPS.map(group => ({ id: group.id, icons: group.icons }))
    const terms = normalized.split(/\s+/)
    const matches = icon => {
        const haystack = normalize([icon.key.replace('ph-', '').replaceAll('-', ' '), icon.pt, icon.en, icon.es].join(' '))
        return terms.every(term => haystack.includes(term))
    }
    return ICON_GROUPS
        .map(group => ({ id: group.id, icons: group.icons.filter(matches) }))
        .filter(group => group.icons.length)
}

export function renderIconGroups(container, groups, { groupClass, itemClass, emptyClass, onSelect }) {
    container.innerHTML = ''
    if (!groups.length) {
        const empty = document.createElement('span')
        empty.className = emptyClass
        empty.textContent = I18n.t('commonNoResults')
        container.appendChild(empty)
        return
    }
    for (const group of groups) {
        const header = document.createElement('span')
        header.className = groupClass
        header.textContent = iconGroupLabel(group.id)
        container.appendChild(header)
        for (const icon of group.icons) {
            const label = iconLabel(icon)
            const btn = document.createElement('button')
            btn.type = 'button'
            btn.className = itemClass
            btn.title = label
            btn.setAttribute('aria-label', label)
            btn.innerHTML = `<i class="ph ${icon.key}"></i>`
            btn.addEventListener('click', () => onSelect(icon, label))
            container.appendChild(btn)
        }
    }
}

export class IconPicker {
    static _current = null
    static _onSelect = null
    static _languageBound = false

    static init(onSelect) {
        const trigger    = document.getElementById('icon-picker-trigger')
        const dialog     = document.getElementById('icon-picker-dropdown')
        const searchInput = document.getElementById('icon-picker-search')
        if (!trigger || !dialog) return

        IconPicker._onSelect = onSelect
        IconPicker._renderGrid(filterIconGroups(''), onSelect)

        trigger.addEventListener('click', (e) => {
            e.stopPropagation()
            if (dialog.open) {
                dialog.close()
                trigger.setAttribute('aria-expanded', 'false')
            } else {
                IconPicker._positionDialog(trigger, dialog)
                dialog.show()
                trigger.setAttribute('aria-expanded', 'true')
                searchInput?.focus()
            }
        })

        searchInput?.addEventListener('input', (e) => {
            IconPicker._renderGrid(filterIconGroups(e.target.value), IconPicker._onSelect)
        })

        document.addEventListener('click', (e) => {
            if (dialog.open && !dialog.contains(e.target) && e.target !== trigger) {
                dialog.close()
                trigger.setAttribute('aria-expanded', 'false')
            }
        })

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && dialog.open) {
                dialog.close()
                trigger.setAttribute('aria-expanded', 'false')
                trigger.focus()
            }
        })

        if (!IconPicker._languageBound) {
            IconPicker._languageBound = true
            I18n.onChange(() => {
                const searchInput2 = document.getElementById('icon-picker-search')
                if (searchInput2) searchInput2.placeholder = I18n.t('searchPlaceholder')
                if (document.getElementById('icon-picker-grid') && IconPicker._onSelect) {
                    IconPicker._renderGrid(filterIconGroups(searchInput2?.value ?? ''), IconPicker._onSelect)
                }
                const hidden = document.getElementById('icon-key-input')
                if (hidden?.value) IconPicker.setValue(hidden.value)
            })
        }
    }

    static _positionDialog(trigger, dialog) {
        const rect = trigger.getBoundingClientRect()
        const spaceBelow = window.innerHeight - rect.bottom - 12
        const spaceAbove = rect.top - 12
        const maxHeight = Math.max(200, Math.min(420, Math.max(spaceBelow, spaceAbove)))
        const openUpwards = spaceBelow < maxHeight && spaceAbove > spaceBelow
        dialog.style.maxHeight = `${maxHeight}px`
        dialog.style.top  = openUpwards ? `${Math.max(8, rect.top - maxHeight - 4)}px` : `${rect.bottom + 4}px`
        dialog.style.left = `${rect.left}px`
        dialog.style.width = `${Math.max(rect.width, 300)}px`
    }

    static setValue(iconKey) {
        const preview = document.getElementById('icon-picker-preview')
        const label   = document.getElementById('icon-picker-label')
        const hidden  = document.getElementById('icon-key-input')
        if (!preview || !label || !hidden) return
        hidden.value = iconKey ?? ''
        if (iconKey) {
            preview.innerHTML = `<i class="ph ${iconKey}" style="font-size:20px"></i>`
            label.textContent = iconLabelByKey(iconKey)
            delete label.dataset.i18n
        } else {
            preview.innerHTML = `<i class="ph ph-tag" style="font-size:20px"></i>`
            label.textContent = I18n.t('iconPickerChoose')
            label.dataset.i18n = 'iconPickerChoose'
        }
    }

    static getValue() {
        return document.getElementById('icon-key-input')?.value || null
    }

    static _renderGrid(groups, onSelect) {
        const grid = document.getElementById('icon-picker-grid')
        if (!grid) return
        renderIconGroups(grid, groups, {
            groupClass: 'icon-picker-group',
            itemClass: 'icon-picker-item',
            emptyClass: 'icon-picker-empty',
            onSelect: icon => {
                onSelect(icon.key)
                const dialog = document.getElementById('icon-picker-dropdown')
                const trigger = document.getElementById('icon-picker-trigger')
                dialog?.close()
                trigger?.setAttribute('aria-expanded', 'false')
                trigger?.focus()
            }
        })
    }
}
