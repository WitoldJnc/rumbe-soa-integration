CREATE TABLE documents.pim_ip_notice (
	id int8 NOT NULL,
	doc_type varchar(255) NOT NULL,
	guid varchar(255) NULL,
	notice_acc_activate varchar(255) NULL,
	notice_acc_num varchar(255) NULL,
	notice_doc_type_lock varchar(255) NULL,
	notice_document_status varchar(255) NULL,
	notice_type_stopped varchar(255) NULL,
	org_guid varchar(255) NOT NULL,
	upd_dt timestamp NULL,
	CONSTRAINT pim_ip_notice_pkey PRIMARY KEY (id)
);