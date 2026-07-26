import re

with open('.github/workflows/cicd.yml', 'r') as f:
    content = f.read()

match = re.search(r'^(\s*)workflow_dispatch:\s*$', content, re.MULTILINE)
if not match:
    print("ERROR: Could not find 'workflow_dispatch:'")
else:
    indent = match.group(1)
    print(f"Found 'workflow_dispatch:' with indent: {repr(indent)}")

    inputs_block = (
        f"\n{indent} inputs:\n"
        f"{indent}  image_tag:\n"
        f"{indent}   description: 'Docker image tag to deploy (defaults to latest)'\n"
        f"{indent}   required: false\n"
        f"{indent}   default: 'latest'\n"
        f"{indent}   type: string\n"
    )

    pos = match.end()
    content = content[:pos] + inputs_block + content[pos:]

    with open('.github/workflows/cicd.yml', 'w') as f:
        f.write(content)

    print("Inserted inputs block successfully!")