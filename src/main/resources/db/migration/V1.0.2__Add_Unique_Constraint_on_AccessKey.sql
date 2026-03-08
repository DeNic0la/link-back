DELETE FROM SecuredLink;
ALTER TABLE SecuredLink ADD CONSTRAINT UK_SecuredLink_accessKey UNIQUE (accessKey);
