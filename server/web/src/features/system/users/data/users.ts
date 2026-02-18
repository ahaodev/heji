import {faker} from '@faker-js/faker'

// Set a fixed seed for consistent data generation
faker.seed(67890)

export const users = Array.from({length: 500}, () => {
    const firstName = faker.person.firstName()
    const lastName = faker.person.lastName()
    const username = faker.internet.username({firstName, lastName}).toLowerCase()

    return {
        id: faker.string.uuid(),
        username,
        email: faker.internet.email({firstName}).toLowerCase(),
        phone: faker.phone.number({style: 'international'}),
        status: faker.helpers.arrayElement([
            'active',
            'inactive',
            'invited',
            'suspended',
        ]),
        created_at: faker.date.past(),
        updated_at: faker.date.recent(),
    }
})
